(ns printpressmfg.cfd-test
  "Pure-function assertions on the purge-flow model.

  These run on the DEFAULT classpath (`clojure -M:test`) with no solver
  present -- they check the problem SETUP and the sample-to-optics
  post-processing. The companion `test-cfd/printpressmfg/
  cfd_solve_test.cljc` (alias `:cfd`) runs the real lattice-Boltzmann
  solve and feeds genuine solved samples through the same functions."
  (:require [clojure.test :refer [deftest is testing]]
            [printpressmfg.cfd :as cfd]
            [printpressmfg.facts :as f]))

(def scanner (f/unit-type-by-id :unit/arf-immersion-lithography-scanner))

;; ----------------------------- problem setup -----------------------------

(deftest approach-velocity-is-the-worst-case-sum
  ;; 600 mm/s scan + 0.45 m/s purge, opposed -> 1.05 m/s.
  (is (< (Math/abs (- (cfd/approach-velocity-m-s scanner) 1.05)) 1e-9)))

(deftest reynolds-number-lands-in-the-modelled-regime
  ;; Re = U*D/nu = 1.05 * 0.045 / 1.53e-5 ~ 3.1e3. Well inside the range
  ;; a D2Q9 BGK solver treats meaningfully, and firmly turbulent-wake
  ;; enough that "is the flow separated?" is a real question.
  (let [re (cfd/reynolds-number scanner)]
    (is (< 2500.0 re 3500.0))))

(deftest reynolds-number-scales-with-speed-and-size
  (is (> (cfd/reynolds-number (assoc scanner :stage-scan-speed-mm-s 1200.0))
         (cfd/reynolds-number scanner)))
  (is (> (cfd/reynolds-number (assoc scanner :stage-frontal-height-mm 90.0))
         (cfd/reynolds-number scanner))))

;; ----------------------------- solver stability envelope -----------------------------

(deftest max-stable-reynolds-scales-linearly-with-body-height
  ;; Re_max = 15 * body-h, from tau_min = 0.51 and u0 = 0.05.
  (is (< (Math/abs (- (cfd/max-stable-reynolds 24) 360.0)) 1e-9))
  (is (< (Math/abs (- (cfd/max-stable-reynolds 48) 720.0)) 1e-9)))

(deftest cells-for-reynolds-inverts-max-stable-reynolds
  (let [re (cfd/reynolds-number scanner)
        cells (cfd/cells-for-reynolds re)]
    (is (>= (cfd/max-stable-reynolds cells) re)
        "the returned cell count must actually be sufficient")
    (is (< (cfd/max-stable-reynolds (dec cells)) re)
        "and must be the SMALLEST sufficient count")))

(deftest the-physical-reynolds-number-is-not-solvable-at-test-grid-sizes
  ;; This is the measured constraint that forced the reduced-Reynolds
  ;; design, asserted so nobody quietly "fixes" the clamp later and
  ;; reintroduces the NaN divergence.
  (let [spec (cfd/lattice-spec scanner)]
    (is (false? (:reynolds-matched? spec)))
    (is (< (:re spec) (:physical-reynolds spec)))
    (is (> (:cells-for-match spec) 200)
        "matching would need a >200-cell body, i.e. an infeasible grid")))

(deftest a-slow-enough-flow-is-reynolds-matched
  ;; Guard against :reynolds-matched? being permanently false: a much
  ;; gentler flow must actually match.
  (let [slow (assoc scanner :stage-scan-speed-mm-s 30.0
                    :purge-face-velocity-m-s 0.02)
        spec (cfd/lattice-spec slow {:body-h 48})]
    (is (true? (:reynolds-matched? spec)))
    (is (< (Math/abs (- (:re spec) (:physical-reynolds spec))) 1e-9))))

(deftest solver-reynolds-never-exceeds-the-stability-ceiling
  (doseq [bh [12 16 24 48]]
    (testing (str "body-h " bh)
      (let [spec (cfd/lattice-spec scanner {:body-h bh})]
        (is (<= (:re spec) (cfd/max-stable-reynolds bh)))))))

(deftest lattice-spec-preserves-the-physical-duct-to-stage-ratio
  ;; 120 mm duct over a 45 mm stage -> 2.667. With body-h = 24 cells the
  ;; channel must be 64 cells, so the solver sees the real confinement.
  (let [spec (cfd/lattice-spec scanner)]
    (is (= 24 (:body-h spec)))
    (is (= 64 (:ny spec)))
    (is (< (Math/abs (- (/ (double (:ny spec)) (:body-h spec))
                        (/ (:purge-duct-height-mm scanner)
                           (:stage-frontal-height-mm scanner))))
           0.02))))

(deftest lattice-spec-geometry-fits-inside-the-domain
  (let [{:keys [nx ny x0 body-w body-h]} (cfd/lattice-spec scanner)]
    (is (< (+ x0 body-w) nx) "body must not run off the outflow boundary")
    (is (< body-h ny) "body must not span the channel")
    (is (pos? x0) "inflow boundary must stand off the stagnation region")))

(deftest blockage-ratio-is-acceptable
  ;; 24/64 = 0.375, under the 0.4 threshold where channel walls rather
  ;; than the body would dominate the measured drag.
  (let [spec (cfd/lattice-spec scanner)]
    (is (< (Math/abs (- (:blockage-ratio spec) 0.375)) 1e-9))
    (is (true? (cfd/blockage-acceptable? spec)))))

(deftest an-over-confined-grid-is-rejected
  ;; Guard against `blockage-acceptable?` being vacuously true: a stage
  ;; nearly filling the duct must be refused.
  (let [choked (cfd/lattice-spec (assoc scanner :purge-duct-height-mm 50.0))]
    (is (false? (cfd/blockage-acceptable? choked)))))

(deftest wake-probe-line-sits-downstream-of-the-body
  (let [spec (cfd/lattice-spec scanner)
        x (cfd/wake-probe-line spec)]
    (is (> x (+ (:x0 spec) (:body-w spec))) "probe must be behind the body")
    (is (< x (:nx spec)) "probe must be inside the domain")))

(deftest resolution-knob-scales-the-whole-grid
  (let [coarse (cfd/lattice-spec scanner {:body-h 12})
        fine (cfd/lattice-spec scanner {:body-h 48})]
    (is (< (:nx coarse) (:nx fine)))
    (is (< (:ny coarse) (:ny fine)))
    ;; blockage is a physical ratio -- it must NOT change with resolution
    (is (< (Math/abs (- (:blockage-ratio coarse) (:blockage-ratio fine))) 0.02))))

;; ----------------------------- post-processing -----------------------------

(deftest uniformity-summarises-a-profile
  (let [u (cfd/uniformity [0.04 0.05 0.06])]
    (is (= 3 (:n u)))
    (is (< (Math/abs (- (:mean u) 0.05)) 1e-9))
    (is (< (Math/abs (- (:peak-to-peak u) 0.02)) 1e-9))
    (is (< (Math/abs (- (:non-uniformity-pct u) 40.0)) 1e-6))
    (is (false? (:recirculation? u)))))

(deftest uniformity-flags-reversed-flow
  (is (true? (:recirculation? (cfd/uniformity [0.05 -0.01 0.04]))))
  (is (false? (:recirculation? (cfd/uniformity [0.05 0.01 0.04])))))

(deftest uniformity-of-a-flat-profile-is-zero
  (let [u (cfd/uniformity [0.05 0.05 0.05])]
    (is (< (Math/abs (:non-uniformity-pct u)) 1e-12))))

(deftest uniformity-of-nothing-is-nil
  (is (nil? (cfd/uniformity []))))

(deftest pressure-coefficient-normalises-out-the-lattice-velocity
  ;; The whole point of going through Cp: the same PHYSICAL flow solved
  ;; at a different lattice velocity must give the same Cp. Lattice
  ;; density fluctuation scales with u0^2, so halving u0 quarters the
  ;; density fluctuation -- and Cp must come out unchanged.
  (let [cp-a (cfd/pressure-coefficient-range [0.9996 1.0004] 0.05)
        cp-b (cfd/pressure-coefficient-range [0.9999 1.0001] 0.025)]
    (is (< (Math/abs (- (:cp-peak-to-peak cp-a) (:cp-peak-to-peak cp-b))) 1e-9))))

(deftest pressure-coefficient-of-a-uniform-field-is-zero
  (let [cp (cfd/pressure-coefficient-range [1.0 1.0 1.0] 0.05)]
    (is (< (Math/abs (:cp-peak-to-peak cp)) 1e-12))))

(deftest optical-path-difference-is-sub-nanometre-but-not-negligible
  ;; Chain check at Cp = 1.0 over a 0.5 m beam run:
  ;;   dp   = 1.0 * 0.5 * 1.20 * 1.05^2      = 0.6615 Pa
  ;;   drho/rho = 0.6615 / 101325            = 6.53e-6
  ;;   dn   = 2.7e-4 * 6.53e-6               = 1.76e-9
  ;;   OPD  = 1.76e-9 * 0.5 m                = 0.88 nm
  ;; Sub-nanometre, yet the same order as the 2 nm overlay budget --
  ;; which is exactly why this term is worth computing at all.
  (let [opd (cfd/optical-path-difference-nm scanner 1.0 0.5)]
    (is (< 0.7 opd 1.1))))

(deftest optical-path-difference-scales-with-path-and-pressure
  (let [base (cfd/optical-path-difference-nm scanner 1.0 0.5)
        long-path (cfd/optical-path-difference-nm scanner 1.0 1.0)
        strong (cfd/optical-path-difference-nm scanner 2.0 0.5)]
    (is (< (Math/abs (- long-path (* 2.0 base))) 1e-9))
    (is (< (Math/abs (- strong (* 2.0 base))) 1e-9)))
  ;; and quadratically with approach velocity (dynamic pressure)
  (let [fast (cfd/optical-path-difference-nm
              (assoc scanner :stage-scan-speed-mm-s 1650.0) 1.0 0.5) ; U = 2.1
        base (cfd/optical-path-difference-nm scanner 1.0 0.5)]
    (is (< (Math/abs (- fast (* 4.0 base))) 1e-6))))

;; ----------------------------- the assembled report -----------------------------

(def fake-solved
  "Stand-in for a solved run, shaped exactly as `test-cfd`'s real solve
  produces. Used here to assert the report's WIRING; the real numbers
  come from the alias-gated solver test."
  {:cd 2.1
   :u0 0.05
   :spec (cfd/lattice-spec scanner)
   :ux-samples [0.048 0.052 0.041 0.055 0.050]
   :rho-samples [0.99975 1.00025 1.00010]})

(deftest assess-produces-a-complete-report
  (let [r (cfd/assess scanner fake-solved)]
    (is (= :unit/arf-immersion-lithography-scanner (:unit-type-id r)))
    (is (= 2.1 (:sectional-cd r)))
    (is (some? (:uniformity r)))
    (is (some? (:pressure-coefficient r)))
    (is (some? (:optical-path-difference-nm r)))
    (is (= 2.0 (:overlay-budget-nm r)))
    (is (some? (:within-budget? r)))
    (is (string? (:caveat r)))
    (testing "the caveat must keep refusing to call this a bound"
      (is (re-find #"NOT a bound" (:caveat r)))
      (is (re-find #"thermal term" (:caveat r)))
      (is (nil? (re-find #"[Ll]ower bound" (:caveat r)))
          "the old 'lower bound' wording was wrong -- the density term is
           over-stated while the thermal term is absent, so the errors run
           in both directions"))))

(deftest a-reduced-reynolds-run-says-so-in-its-own-report
  ;; The report must never let a caller mistake a clamped solve for a
  ;; Reynolds-matched one -- both the flag and the prose have to carry it.
  (let [r (cfd/assess scanner fake-solved)]
    (is (false? (:reynolds-matched? r)))
    (is (< (:solver-reynolds r) (:reynolds-number r)))
    (is (re-find #"reduced Reynolds" (:caveat r)))
    (is (re-find #"NOT Reynolds-matched" (:caveat r)))))

(deftest a-matched-run-does-not-carry-the-reduced-reynolds-warning
  ;; Guard against the warning being unconditional boilerplate.
  (let [slow (assoc scanner :stage-scan-speed-mm-s 30.0
                    :purge-face-velocity-m-s 0.02)
        r (cfd/assess slow (assoc fake-solved
                                  :spec (cfd/lattice-spec slow {:body-h 48})))]
    (is (true? (:reynolds-matched? r)))
    (is (nil? (re-find #"reduced Reynolds" (:caveat r))))
    (is (re-find #"NOT a bound" (:caveat r))
        "the other caveats must still travel with it")))

(deftest budget-verdict-tracks-the-budget
  (let [r (cfd/assess scanner fake-solved)
        opd (:optical-path-difference-nm r)]
    (is (= (< opd 2.0) (:within-budget? r)))
    (is (< (Math/abs (- (:budget-fraction r) (/ opd 2.0))) 1e-12))
    ;; tighten the budget below the computed OPD -> verdict must flip
    (let [tight (cfd/assess (assoc scanner :overlay-budget-nm (/ opd 2.0))
                            fake-solved)]
      (is (false? (:within-budget? tight))))))

(deftest assess-honours-the-path-length-argument
  (let [short-path (cfd/assess scanner fake-solved 0.25)
        long-path (cfd/assess scanner fake-solved 1.0)]
    (is (< (:optical-path-difference-nm short-path)
           (:optical-path-difference-nm long-path)))))
