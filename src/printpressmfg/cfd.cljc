(ns printpressmfg.cfd
  "Purge-gas flow model for `printpressmfg.facts/unit-types`'
  `:unit/arf-immersion-lithography-scanner`, expressed as a
  lattice-Boltzmann problem definition plus the pure post-processing
  that turns a solved flow field into an OPTICAL budget number.

  ## What is being modelled, and why it is a real design question

  A projection scanner's wafer stage sweeps at ~0.6 m/s underneath the
  projection optics inside an environmental enclosure that is purged
  with temperature-controlled clean dry air. The scanner's own
  interferometric position metrology looks THROUGH that gas. Gas
  density variation changes the refractive index (Gladstone-Dale),
  which changes the optical path length the interferometer measures,
  which appears directly in the overlay error budget. So 'is the purge
  flow over the moving stage smooth or separated?' is not a
  contamination-control question -- it is a nanometre-budget question.

  This namespace answers it in three pure steps:

    1. `lattice-spec`   -- unit parameters -> a D2Q9 channel problem
                           (duct as the channel, stage as a bluff
                           block), at the correct Reynolds number.
    2. (solve)          -- performed OUTSIDE this ns by `kami-cfd`.
    3. `assess`         -- solved samples -> flow uniformity, pressure
                           coefficient, optical path difference, and a
                           verdict against the unit's overlay budget.

  ## Why this ns has no `:require`

  Deliberately dependency-free (nothing beyond `clojure.core`) so it
  stays on the always-on `src` classpath without putting a solver into
  this repo's governance/actuation dependency graph. `deps.edn`'s
  `:cfd` alias is the ONLY place `io.github.kotoba-lang/kami-engine-cfd`
  is declared, and `test-cfd/printpressmfg/cfd_solve_test.cljc` is what
  actually `:require`s `kami-cfd` and feeds real solved samples into
  `assess`. This mirrors how the sibling isic-2813 repo isolates its
  rendering stack behind a `:visualize` alias (ADR-2800000100).

  ## Why ArF immersion and NOT EUV -- a physics admissibility check

  The obvious 'most advanced' choice for this catalog entry would have
  been an EUV scanner. It was deliberately NOT chosen for the CFD
  slice, because EUV would make this model physically invalid:

    - An EUV scanner's beam path runs in near-vacuum hydrogen at the
      order of a few pascals. At that pressure the molecular mean free
      path is comparable to the flow geometry (Knudsen number well out
      of the continuum regime), and the Navier-Stokes/lattice-Boltzmann
      continuum assumption this solver is built on no longer holds --
      rarefied-gas flow needs DSMC or a Boltzmann solver with a proper
      collision model, not BGK-LBM.
    - An ArF immersion scanner's environmental purge is clean dry air
      at ~1 atm over centimetre-scale geometry. Knudsen number there is
      of order 1e-6 -- solidly continuum, and squarely inside what a
      D2Q9 BGK lattice-Boltzmann solver is a legitimate tool for.

  Picking EUV would have produced numbers that LOOK more impressive and
  are physically meaningless. This is the same 'choose the variant the
  method can honestly compute' judgement ADR-2800000400 recorded when
  it chose powder-bed fusion over directed energy deposition.

  ## Stated limitations (read before citing any number from this ns)

    - 2D. A real stage/duct is three-dimensional; a 2D section
      overstates spanwise-uniform separation and cannot represent
      side-edge vortices. `kami-cfd.d3` (D3Q19) exists and would be the
      upgrade path.
    - This models the DENSITY/PRESSURE term of the refractive-index
      perturbation only. The THERMAL term (dn/dT for air is about
      -9e-7 per K, so a 1 mK non-uniformity produces a refractive-index
      change of the same order as the pressure term computed here) is
      NOT modelled -- an isothermal LBM cannot produce it. A real
      environmental-control budget is dominated by the thermal term.
      Numbers from this ns are therefore a LOWER BOUND on the optical
      disturbance, never a total.
    - The optical path difference is reported as an OPD, not as a
      stage-position error. Mapping OPD to a position error depends on
      interferometer fold geometry (pass count, beam routing) that this
      model does not describe.
    - Dimensions and speeds come from
      `printpressmfg.facts/unit-types`, whose non-optical values are
      explicitly illustrative rather than vendor data.")

;; ---------------------------------------------------------------------------
;; Physical constants (all at ~22 C, 1 atm -- cleanroom conditions)
;; ---------------------------------------------------------------------------

(def air-kinematic-viscosity-m2-s
  "Kinematic viscosity of air at ~22 C, 1 atm."
  1.53e-5)

(def air-density-kg-m3
  "Density of air at ~22 C, 1 atm."
  1.20)

(def atmospheric-pressure-pa 101325.0)

(def air-refractivity
  "(n - 1) for air at standard cleanroom conditions in the visible/near-IR
  band a scanner's stage interferometer works in. Used with the
  Gladstone-Dale relation, which states that (n - 1) is proportional to
  gas density -- so a FRACTIONAL density change produces the same
  fractional change in (n - 1)."
  2.7e-4)

(def lattice-sound-speed-squared
  "c_s^2 = 1/3 in D2Q9 lattice units; converts a lattice density
  fluctuation to a lattice pressure fluctuation via p = rho * c_s^2."
  (/ 1.0 3.0))

;; ---------------------------------------------------------------------------
;; Solver stability envelope -- why the solved Reynolds number is not the
;; physical one, stated in code rather than buried in prose
;; ---------------------------------------------------------------------------

(def lattice-inflow-velocity
  "`kami-cfd/lbm-new` fixes the lattice inflow velocity at u0 = 0.05 for
  Mach-number stability. It is a solver constant, not a physical speed."
  0.05)

(def min-relaxation-time
  "BGK stability floor on the relaxation time tau.

  `kami-cfd/lbm-new` sets tau = 0.5 + 3*nu with nu = u0*D/Re, so a HIGH
  Reynolds number at a SMALL body drives tau towards 0.5, where the BGK
  collision operator loses positivity and the solve diverges to NaN.
  0.51 is the practical floor -- measured on this very model: at the
  physical Re (~3.1e3) with a 16-cell body, tau = 0.50078 and every
  sample came back NaN."
  0.51)

(defn max-stable-reynolds
  "Highest Reynolds number a body `body-h` lattice cells tall can be
  solved at before tau falls under `min-relaxation-time`.

  Works out to 15 * body-h, so Reynolds-matching a real flow costs grid
  cells LINEARLY -- and, because step count scales with domain length
  too, wall-clock super-linearly."
  [body-h]
  (let [nu-min (/ (- min-relaxation-time 0.5) 3.0)]
    (/ (* lattice-inflow-velocity (double body-h)) nu-min)))

(defn cells-for-reynolds
  "Body height in lattice cells needed to solve `re` stably -- the
  inverse of `max-stable-reynolds`. Use it to report the true cost of a
  Reynolds-matched run instead of silently not doing one."
  [re]
  (let [nu-min (/ (- min-relaxation-time 0.5) 3.0)]
    (long (Math/ceil (/ (* re nu-min) lattice-inflow-velocity)))))

;; ---------------------------------------------------------------------------
;; Step 1 -- unit parameters to a solvable lattice problem
;; ---------------------------------------------------------------------------

(defn approach-velocity-m-s
  "Relative gas speed seen by the wafer stage, taken as the WORST CASE:
  the stage scanning directly into the purge flow, so the scan speed
  and the purge face velocity add. Choosing the worst case (rather than
  the vector average over a scan cycle) is deliberate -- this model is
  used to bound an error budget, and a budget bound that assumed the
  favourable scan direction would be useless."
  [{:keys [stage-scan-speed-mm-s purge-face-velocity-m-s]}]
  (+ (/ stage-scan-speed-mm-s 1000.0) purge-face-velocity-m-s))

(defn reynolds-number
  "Re = U * D / nu, with D the stage's frontal height (the bluff
  dimension that sets the wake) and U from `approach-velocity-m-s`."
  [unit]
  (let [u (approach-velocity-m-s unit)
        d (/ (:stage-frontal-height-mm unit) 1000.0)]
    (/ (* u d) air-kinematic-viscosity-m2-s)))

(defn lattice-spec
  "Unit -> the D2Q9 channel problem to hand to `kami-cfd`.

  The channel is the purge duct; the bluff block is the wafer stage.
  The duct-to-stage height ratio is preserved exactly from the unit's
  own `:purge-duct-height-mm` / `:stage-frontal-height-mm`, so the
  BLOCKAGE RATIO the solver sees is the physical one rather than an
  artefact of grid choice.

  `body-h` (stage height in lattice cells) is the resolution knob;
  everything else scales from it. `:steps` is sized so the flow
  convects through the domain several times before the drag average
  over the final 20% of steps is taken.

  ## `:re` is the SOLVER Reynolds number, and may be below the physical one

  The physical flow sits at Re ~ 3.1e3, which `max-stable-reynolds`
  shows needs a body ~206 cells tall -- a ~2060 x 549 grid run for
  ~165k steps. That is not a run this repo's test suite can carry, so
  `:re` is CLAMPED to what the chosen grid can solve stably, and the
  spec reports the clamp openly:

    `:physical-reynolds`   the real flow's Re
    `:re`                  what the solver is actually given
    `:reynolds-matched?`   false whenever those differ
    `:cells-for-match`     what a matched run would have cost

  Why a reduced-Reynolds run is still informative HERE specifically:
  the stage is a SHARP-EDGED rectangular block, so its separation
  points are pinned at the leading corners by geometry rather than set
  by the boundary layer. Sharp-edged bluff bodies are famously
  Reynolds-insensitive in sectional Cp/Cd across decades of Re, which
  is exactly why the post-processing routes through the dimensionless
  `pressure-coefficient-range` instead of through raw lattice values.
  A ROUNDED body would not survive this argument -- its separation
  point moves with Re, and a reduced-Re run would be misleading.

  This is a stated approximation, not a Reynolds-matched result, and
  `assess` propagates `:reynolds-matched?` into its report so no caller
  can quote the number as though it were one."
  ([unit] (lattice-spec unit {}))
  ([unit {:keys [body-h streamwise-lengths] :or {body-h 24 streamwise-lengths 5}}]
   (let [duct-mm (:purge-duct-height-mm unit)
         stage-mm (:stage-frontal-height-mm unit)
         ;; preserve the physical duct/stage height ratio
         ny (long (Math/round (* (double body-h) (/ duct-mm stage-mm))))
         ;; a wafer stage is much wider than it is tall; 3:1 streamwise
         ;; aspect keeps it a bluff body while giving the wake somewhere
         ;; to reattach.
         body-w (* 3 body-h)
         ;; leading edge two body-heights downstream of the inlet, so the
         ;; inflow boundary is not sitting on the stagnation region
         x0 (* 2 body-h)
         nx (+ x0 body-w (* streamwise-lengths body-h))
         physical-re (reynolds-number unit)
         re-ceiling (max-stable-reynolds body-h)
         solver-re (min physical-re re-ceiling)]
     {:nx nx :ny ny :x0 x0 :body-w body-w :body-h body-h
      :re solver-re
      :physical-reynolds physical-re
      :max-stable-reynolds re-ceiling
      :reynolds-matched? (>= re-ceiling physical-re)
      :cells-for-match (cells-for-reynolds physical-re)
      ;; enough steps for several domain flow-throughs at u0 = 0.05
      :steps (long (Math/round (* 4.0 (/ (double nx) lattice-inflow-velocity))))
      :blockage-ratio (/ (double body-h) (double ny))
      :approach-velocity-m-s (approach-velocity-m-s unit)})))

(defn blockage-acceptable?
  "A channel blockage ratio above ~0.4 makes the walls, not the body,
  dominate the drag -- the result stops being a meaningful sectional Cd.
  Reported so a caller can reject an over-confined grid rather than
  silently quote a wall-dominated number."
  [{:keys [blockage-ratio]}]
  (< blockage-ratio 0.4))

(defn wake-probe-line
  "x column (in lattice cells) one body-length downstream of the stage's
  trailing edge, where the wake is developed but not yet damped by the
  outflow boundary. This is the line `assess` expects samples along --
  it stands in for the metrology beam path crossing the disturbed gas."
  [{:keys [x0 body-w body-h nx]}]
  (min (dec nx) (+ x0 body-w body-h)))

;; ---------------------------------------------------------------------------
;; Step 3 -- solved samples to an optical budget number
;; ---------------------------------------------------------------------------

(defn uniformity
  "Streamwise-velocity uniformity across a probe line.

  `ux-samples` is a seq of lattice x-velocities (solid cells already
  removed by the caller). Returns mean/min/max plus
  `:non-uniformity-pct`, the peak-to-peak spread as a percentage of the
  mean -- the single number that says how far the purge flow departs
  from the flat profile the enclosure is designed to deliver."
  [ux-samples]
  (let [xs (vec ux-samples)
        n (count xs)]
    (when (pos? n)
      (let [mean (/ (reduce + xs) (double n))
            lo (reduce min xs)
            hi (reduce max xs)]
        {:n n
         :mean mean
         :min lo
         :max hi
         :peak-to-peak (- hi lo)
         :non-uniformity-pct (if (zero? mean)
                               ##Inf
                               (* 100.0 (/ (- hi lo) (Math/abs mean))))
         ;; a negative minimum means reversed flow -> recirculation
         :recirculation? (neg? lo)}))))

(defn pressure-coefficient-range
  "Peak-to-peak pressure coefficient Cp from lattice DENSITY samples.

      Cp = (rho - rho_ref) * c_s^2 / (0.5 * rho_ref * u0^2)

  Cp is dimensionless and Reynolds-similar, which is exactly why the
  conversion has to go through it: the solver's lattice velocity
  (u0 = 0.05) is a numerical convenience with no physical Mach number,
  so lattice density fluctuations must NEVER be read as physical
  density fluctuations directly. Cp transfers; raw lattice rho does
  not."
  [rho-samples u0]
  (let [xs (vec rho-samples)]
    (when (seq xs)
      (let [lo (reduce min xs)
            hi (reduce max xs)
            q (* 0.5 1.0 u0 u0)]
        {:cp-min (/ (* (- lo 1.0) lattice-sound-speed-squared) q)
         :cp-max (/ (* (- hi 1.0) lattice-sound-speed-squared) q)
         :cp-peak-to-peak (/ (* (- hi lo) lattice-sound-speed-squared) q)}))))

(defn optical-path-difference-nm
  "Peak-to-peak optical path difference across the metrology path, in
  nanometres, from a peak-to-peak pressure coefficient.

  Chain, each step a standard relation:

    dp     = Cp * 0.5 * rho_air * U^2        (definition of Cp)
    drho/rho = dp / p_atm                    (isothermal, low-speed)
    dn     = (n-1) * drho/rho                (Gladstone-Dale: n-1 ~ rho)
    OPD    = dn * L                          (optical path through L)

  `path-length-m` is how far the metrology beam runs through the
  disturbed gas."
  [unit cp-peak-to-peak path-length-m]
  (let [u (approach-velocity-m-s unit)
        dp (* cp-peak-to-peak 0.5 air-density-kg-m3 u u)
        drho-over-rho (/ dp atmospheric-pressure-pa)
        dn (* air-refractivity drho-over-rho)]
    (* dn path-length-m 1e9)))

(defn assess
  "Full report from a solved run.

  `solved` is what the alias-gated solver test produces:
    {:cd            sectional drag coefficient of the stage
     :u0            the solver's lattice inflow velocity
     :ux-samples    lattice x-velocities along `wake-probe-line`
     :rho-samples   lattice densities along the same line
     :spec          (optional) the `lattice-spec` the run used, so the
                    report can state whether it was Reynolds-matched}

  `path-length-m` defaults to 0.5 m, an illustrative stand-in for the
  stage-interferometer beam run inside the enclosure.

  The verdict compares the DENSITY-term OPD against the unit's overlay
  budget. Per this ns's stated limitations that is a LOWER BOUND on the
  optical disturbance -- so `:within-budget? true` means 'this term
  alone does not break the budget', never 'the tool meets overlay'.

  Note that the OPD is built from the DIMENSIONLESS pressure coefficient
  combined with the PHYSICAL approach velocity. That split is what lets
  a reduced-Reynolds solve still say something useful about the real
  machine (see `lattice-spec`), and it is why lattice values are never
  read as physical ones directly."
  ([unit solved] (assess unit solved 0.5))
  ([unit {:keys [cd u0 ux-samples rho-samples spec]} path-length-m]
   (let [uni (uniformity ux-samples)
         cp (pressure-coefficient-range rho-samples u0)
         opd (when cp
               (optical-path-difference-nm unit (:cp-peak-to-peak cp) path-length-m))
         budget (:overlay-budget-nm unit)
         matched? (:reynolds-matched? spec)]
     {:unit-type-id (:id unit)
      :reynolds-number (reynolds-number unit)
      :solver-reynolds (:re spec)
      :reynolds-matched? matched?
      :approach-velocity-m-s (approach-velocity-m-s unit)
      :sectional-cd cd
      :uniformity uni
      :pressure-coefficient cp
      :path-length-m path-length-m
      :optical-path-difference-nm opd
      :overlay-budget-nm budget
      :within-budget? (when (and opd budget) (< opd budget))
      :budget-fraction (when (and opd budget (pos? budget)) (/ opd budget))
      ;; Single-line on purpose: this is machine-readable report data, so
      ;; it must not carry source indentation into whatever consumes it.
      :caveat
      (str "Density/pressure term only; the thermal term (dn/dT ~ -9e-7 /K) "
           "is not modelled and typically dominates. 2D section. "
           "Lower bound, not a total optical budget."
           (when (false? matched?)
             (str " Solved at reduced Reynolds (" (long (:re spec))
                  " vs physical " (long (:physical-reynolds spec))
                  "); sharp-edged-body Cp similarity assumed, NOT"
                  " Reynolds-matched.")))})))
