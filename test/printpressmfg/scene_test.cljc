(ns printpressmfg.scene-test
  "Structural assertions on the render-IR `printpressmfg.scene` emits.

  These run on the DEFAULT classpath (`clojure -M:test`) and require no
  renderer -- they check the shape and the physical consistency of the
  scene. The companion `test-visualize/printpressmfg/
  scene_render_test.cljc` (alias `:visualize`) checks the same output
  against the real `kami.webgpu.ir` API and a real browser."
  (:require [clojure.test :refer [deftest is testing]]
            [printpressmfg.facts :as f]
            [printpressmfg.scene :as scene]))

(def scanner (f/unit-type-by-id :unit/arf-immersion-lithography-scanner))
(def ir (scene/unit->render-ir scanner))

;; ----------------------------- render-IR shape -----------------------------

(deftest render-ir-has-globals-and-instances
  (is (map? (:globals ir)))
  (is (sequential? (:instances ir)))
  (is (pos? (count (:instances ir)))))

(deftest globals-carry-a-camera-and-a-sky
  (let [g (:globals ir)]
    (is (= 3 (count (:eye g))))
    (is (= 3 (count (:target g))))
    (is (map? (:sky g)))
    (is (= 3 (count (get-in g [:sky :horizon]))))
    (is (= 3 (count (get-in g [:sky :sun-dir]))))
    (is (= 3 (count (get-in g [:sky :sun]))))))

(deftest every-instance-conforms-to-the-v1-instance-shape
  ;; Mirrors what `kami.webgpu.ir/valid?` checks, so a failure here is
  ;; caught without the renderer on the classpath.
  (doseq [[i inst] (map-indexed vector (:instances ir))]
    (testing (str "instance " i)
      (is (vector? (:pos inst)))
      (is (vector? (:color inst)))
      (is (vector? (:size inst)))
      (is (= 3 (count (:pos inst))))
      (is (= 3 (count (:color inst))))
      (is (= 3 (count (:size inst))))
      (is (contains? #{:box :cylinder} (:geo inst)))
      (is (every? #(<= 0.0 % 1.0) (:color inst))
          "colours are normalised 0..1")
      (is (every? pos? (:size inst))
          "no zero/negative extents"))))

(deftest instance-count-is-the-documented-eleven
  (is (= 11 (scene/instance-count scanner)))
  (is (= 11 (count (:instances ir)))))

(deftest material-parameters-are-in-range
  (doseq [inst (:instances ir)]
    (is (<= 0.0 (:metallic inst) 1.0))
    (is (<= 0.0 (:roughness inst) 1.0))
    (is (<= 0.0 (:emissive inst) 1.0))))

;; ----------------------------- physical consistency -----------------------------

(deftest nothing-sinks-below-the-ground-plane
  ;; Every primitive is centred at :pos with :size extents, so the lowest
  ;; point of instance i is pos_y - size_y/2. The tool stands on y = 0.
  (doseq [[i inst] (map-indexed vector (:instances ir))]
    (testing (str "instance " i)
      (is (>= (- (nth (:pos inst) 1) (/ (nth (:size inst) 1) 2.0)) -1e-9)))))

(deftest wafer-diameter-is-traceable-to-the-catalog
  ;; The wafer cylinder's footprint must be exactly the catalog's
  ;; :wafer-diameter-mm -- this is one of the three dimensions the scene
  ;; is allowed to claim traceability for.
  (let [wafer (first (filter #(= :cylinder (:geo %)) (:instances ir)))
        expected-m (/ (:wafer-diameter-mm scanner) 1000.0)]
    ;; the wafer is the FIRST cylinder placed nearest the stage top;
    ;; find it by matching the expected footprint instead of by index
    (is (some (fn [inst]
                (and (= :cylinder (:geo inst))
                     (< (Math/abs (- (nth (:size inst) 0) expected-m)) 1e-9)))
              (:instances ir))
        "a cylinder with exactly the catalog wafer diameter must exist")
    (is (some? wafer))))

(deftest the-rendered-purge-gap-is-the-gap-the-cfd-model-solves
  ;; This is the coupling assertion. `printpressmfg.cfd/lattice-spec`
  ;; builds its channel from :purge-duct-height-mm measured from the
  ;; stage TOP; the plenum instance must sit exactly that far above the
  ;; stage top, or the picture and the simulation are describing
  ;; different machines.
  (let [dims (scene/unit->dimensions scanner)
        [_ stage-h _] (:stage dims)
        [_ frame-h _] (:frame dims)
        [_ plenum-h _] (:plenum dims)
        stage-top (+ frame-h stage-h)
        plenum (nth (:instances ir) 7)              ; purge plenum
        plenum-bottom (- (nth (:pos plenum) 1) (/ plenum-h 2.0))
        gap (- plenum-bottom stage-top)]
    (is (< (Math/abs (- gap (/ (:purge-duct-height-mm scanner) 1000.0))) 1e-9)
        "plenum face must stand :purge-duct-height-mm above the stage top")))

(deftest stage-height-is-traceable-to-the-catalog
  ;; The bluff-body height the CFD slice uses is the stage box's own
  ;; height -- assert they are the same number.
  (let [dims (scene/unit->dimensions scanner)
        [_ stage-h _] (:stage dims)]
    (is (< (Math/abs (- stage-h (/ (:stage-frontal-height-mm scanner) 1000.0))) 1e-9))))

(deftest scene-scales-with-wafer-size
  ;; A 200 mm variant must produce a smaller stage footprint -- proves
  ;; the builder is parametric rather than a hardcoded model.
  (let [small (scene/unit->dimensions (assoc scanner :wafer-diameter-mm 200.0))
        big (scene/unit->dimensions scanner)]
    (is (< (nth (:stage small) 0) (nth (:stage big) 0)))
    (is (< (:wafer-r small) (:wafer-r big)))))

(deftest builder-is-pure
  (is (= ir (scene/unit->render-ir scanner))))
