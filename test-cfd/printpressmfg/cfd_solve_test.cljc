(ns printpressmfg.cfd-solve-test
  "REAL lattice-Boltzmann solve of the wafer-stage purge flow.

  Run with:  clojure -M:cfd

  Deliberately outside the default `test` directory: this is the ONLY
  place `kami-cfd` is on the classpath, so a bare `clojure -M:test`
  never needs a CFD solver (see `deps.edn`'s `:cfd` alias and the ns
  docstring of `printpressmfg.cfd`).

  What this proves that `test/printpressmfg/cfd_test.cljc` cannot: that
  `printpressmfg.cfd/lattice-spec` actually produces a problem the real
  `kami-cfd` D2Q9 solver accepts and converges on, and that real solved
  samples flowing through `assess` land at a physically sane place --
  not merely that the arithmetic is self-consistent."
  (:require [clojure.test :refer [deftest is testing]]
            [kami-cfd :as lbm]
            [printpressmfg.cfd :as cfd]
            [printpressmfg.facts :as f]))

(def scanner (f/unit-type-by-id :unit/arf-immersion-lithography-scanner))

(defn- nan?
  "Portable NaN test. `Double/isNaN` is JVM-only and would break this
  `.cljc` under ClojureScript; NaN is the only value not equal to
  itself, so this works on every runtime."
  [x]
  (not= x x))

(defn solve
  "Run the purge-flow problem for `unit` and return the `:cd`/`:u0`/
  `:ux-samples`/`:rho-samples` map `printpressmfg.cfd/assess` consumes.

  `body-h` is kept modest by default so the suite stays quick; the
  physics assertions below are all qualitative (separation present,
  drag in a bluff-body range), which is what a grid this coarse can
  honestly support."
  ([unit] (solve unit {:body-h 16 :steps 3000}))
  ([unit {:keys [body-h steps]}]
   (let [spec (cfd/lattice-spec unit {:body-h body-h})
         {:keys [nx ny x0 body-w re]} spec
         body (lbm/block nx ny x0 body-w body-h)
         solver (lbm/lbm-new body re)
         ;; step manually so we can read the field back at the end
         [final cd-sum tail]
         (loop [s 0 st solver sum 0.0]
           (if (>= s steps)
             [st sum (max (quot steps 5) 1)]
             (let [[st' drag] (lbm/step st)]
               (recur (inc s) st' (if (>= s (- steps (max (quot steps 5) 1)))
                                    (+ sum drag)
                                    sum)))))
         probe-x (cfd/wake-probe-line spec)
         cells (keep #(lbm/cell-at final probe-x %) (range ny))
         favg (/ cd-sum (double tail))
         u0 (:u0 final)]
     {:spec spec
      :u0 u0
      :cd (/ favg (* 0.5 u0 u0 (double body-h)))
      :ux-samples (mapv second cells)
      :rho-samples (mapv first cells)})))

(def solved (delay (solve scanner)))

;; ----------------------------- the solver accepts our problem -----------------------------

(deftest lattice-spec-is-solvable-by-the-real-solver
  (let [{:keys [spec ux-samples rho-samples]} @solved]
    (is (true? (cfd/blockage-acceptable? spec)))
    (is (pos? (count ux-samples))
        "the wake probe line must return non-solid cells")
    (is (= (count ux-samples) (count rho-samples)))
    (is (every? #(and (number? %) (not (nan? %))) ux-samples)
        "a diverged solve would show up as NaN here")
    (is (every? #(and (pos? %) (not (nan? %))) rho-samples)
        "density must stay positive and finite")))

(deftest the-solve-runs-inside-the-stability-envelope
  ;; The clamp in `lattice-spec` is what keeps this suite off the NaN
  ;; divergence that the physical Reynolds number produces. Assert the
  ;; envelope explicitly so a future edit that removes the clamp fails
  ;; HERE, with a clear reason, rather than as a wall of NaN.
  (let [{:keys [spec]} @solved
        tau (+ 0.5 (* 3.0 (/ (* cfd/lattice-inflow-velocity (:body-h spec))
                             (:re spec))))]
    (is (>= tau cfd/min-relaxation-time)
        (str "relaxation time " tau " is below the BGK stability floor"))
    (is (false? (:reynolds-matched? spec))
        "at this grid size the run is knowingly reduced-Reynolds")))

(deftest solved-drag-is-in-a-bluff-body-range
  ;; A square-backed block in a confined channel sits in the roughly 1-3
  ;; range for sectional Cd. This is a SANITY band, not a validation
  ;; against a reference experiment -- a 2D coarse-grid reduced-Reynolds
  ;; LBM result must not be quoted as a measured drag coefficient.
  (let [cd (:cd @solved)]
    (is (not (nan? cd)) "solve diverged")
    (is (< 0.5 cd 5.0) (str "sectional Cd out of plausible band: " cd))))

(deftest the-wake-is-actually-disturbed
  ;; If the probe line came back with a flat profile, the model would be
  ;; measuring nothing and every downstream optical number would be a
  ;; meaningless zero. Assert the bluff body genuinely perturbs the flow.
  (let [u (cfd/uniformity (:ux-samples @solved))]
    (is (some? u))
    (is (> (:non-uniformity-pct u) 5.0)
        (str "expected a disturbed wake, got " (:non-uniformity-pct u) "%"))))

;; ----------------------------- solved samples through the report -----------------------------

(deftest assess-on-real-solved-samples-produces-a-usable-report
  (let [r (cfd/assess scanner @solved)]
    (testing "report is complete"
      (is (= :unit/arf-immersion-lithography-scanner (:unit-type-id r)))
      (is (number? (:sectional-cd r)))
      (is (number? (:optical-path-difference-nm r)))
      (is (some? (:within-budget? r))))
    (testing "the optical path difference is physically sane"
      ;; Positive (a peak-to-peak spread cannot be negative) and far
      ;; below a micrometre -- a low-speed air flow simply cannot bend
      ;; light by more than that.
      (let [opd (:optical-path-difference-nm r)]
        (is (pos? opd))
        (is (< opd 1000.0)
            (str "OPD of " opd " nm is not credible for a 1 m/s air flow"))))
    (testing "the caveat travels with the number"
      (is (re-find #"NOT a bound" (:caveat r)))
      (is (re-find #"reduced Reynolds" (:caveat r))))))

(deftest report-is-reproducible
  ;; The solver is deterministic (no RNG); the same spec must give the
  ;; same answer, or the numbers in the ADR could not be trusted.
  (let [a (solve scanner {:body-h 12 :steps 800})
        b (solve scanner {:body-h 12 :steps 800})]
    (is (= (:cd a) (:cd b)))
    (is (= (:ux-samples a) (:ux-samples b)))))

(deftest faster-scanning-raises-the-optical-disturbance
  ;; The design question this model exists to answer: does pushing stage
  ;; speed for throughput cost overlay budget? Re changes the flow AND
  ;; the dynamic pressure, so solve both rather than scaling analytically.
  (let [slow (assoc scanner :stage-scan-speed-mm-s 300.0)
        fast (assoc scanner :stage-scan-speed-mm-s 1200.0)
        r-slow (cfd/assess slow (solve slow {:body-h 12 :steps 1200}))
        r-fast (cfd/assess fast (solve fast {:body-h 12 :steps 1200}))]
    (is (> (:optical-path-difference-nm r-fast)
           (:optical-path-difference-nm r-slow))
        "a faster stage must disturb the metrology gas more, not less")))
