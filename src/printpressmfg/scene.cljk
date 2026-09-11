(ns printpressmfg.scene
  "Parametric 3D scene builder for `printpressmfg.facts/unit-types`
  entries, producing data in the kami.webgpu.ir v1 render-IR shape
  (kotoba-lang/webgpu `kami.webgpu.ir`, whose ns docstring is the
  canonical contract):

    {:globals   {:sky {:horizon [...] :sun-dir [...] :sun [...]}
                 :eye [...] :target [...]}
     :instances [{:pos [...] :color [...] :size [...] :yaw t
                  :geo (:box|:cylinder) :metallic m :roughness r
                  :emissive e} ...]}

  Per this workspace's repo-wide 3D rule, all 3D goes through the
  canonical kami-engine stack -- this ns emits that stack's render-IR
  rather than inventing a second scene representation.

  Deliberately dependency-free (no `:require` beyond `clojure.core`) so
  it stays on the always-on `src` classpath without pulling
  kami-engine's rendering stack into this repo's governance/actuation
  dependency graph. `deps.edn`'s `:visualize` alias is the ONLY place
  `io.github.kotoba-lang/webgpu` is declared; `test-visualize/
  printpressmfg/scene_render_test.cljc` is what `:require`s
  `kami.webgpu.ir` / `kami.webgpu.geometry` and exercises this ns's
  output against the real API. This is the isolation pattern
  ADR-2800000100 established for the sibling isic-2813 repo.

  ## The scene is the same machine the CFD slice models

  The purge plenum instance is placed at the unit's own
  `:purge-duct-height-mm` above the wafer stage, and the wafer stage
  box carries the unit's own `:stage-frontal-height-mm`. Those are the
  two dimensions `printpressmfg.cfd/lattice-spec` turns into the
  channel and the bluff body -- so the rendered gap and the simulated
  gap are the same gap, driven from one catalog entry, not two
  independently drifting sets of numbers.

  ## HONESTY / SCOPE

  Every dimension below is an ILLUSTRATIVE parametric primitive derived
  from the catalog entry by the simple formulas in `unit->dimensions`,
  chosen to look like a plausible 300 mm immersion scanner at this
  class. This is NOT a manufacturer spec sheet, NOT a measured value,
  and NOT a CAD-fidelity model -- it is a composition of boxes and
  cylinders. The wafer diameter and the duct/stage heights are the only
  dimensions traceable to catalog values; the rest are proportions."
  )

;; ---- palette ---------------------------------------------------------------
;; Identification-purpose colours for a cleanroom tool, not an attempt to
;; encode any specific manufacturer's finish.

(def frame-color
  "Dark granite/cast base. Precision stages are mounted on a heavy
  vibration-isolated base; dark grey stone or dark-painted cast iron is
  the usual appearance."
  [0.24 0.24 0.26])

(def panel-color
  "Off-white cleanroom equipment panelling -- the near-universal exterior
  finish for fab tools, chosen for particle visibility and light return."
  [0.88 0.89 0.90])

(def optics-color
  "Light grey projection-optics column housing, a shade darker than the
  outer panels so the column reads as a separate assembly."
  [0.70 0.72 0.75])

(def wafer-color
  "Polished 300 mm silicon: a dark blue-grey mirror. Rendered with high
  metallic / low roughness because a finished wafer surface is
  specular, which is why it is a usable optical reference at all."
  [0.28 0.31 0.38])

(def stage-color
  "Wafer-stage body -- light structural metal, distinct from both the
  dark base and the off-white panels."
  [0.62 0.64 0.68])

(def immersion-color
  "The water-filled immersion hood between the final lens element and
  the wafer. Pale blue, purely to make the immersion gap legible in the
  render -- ultrapure water is of course colourless."
  [0.62 0.80 0.92])

(def plenum-color
  "Purge-air plenum/laminar-flow face above the stage. Pale grey; the
  face this instance represents is the inflow boundary of the channel
  `printpressmfg.cfd` solves."
  [0.80 0.83 0.86])

(def source-color
  "ArF excimer laser source cabinet -- a separate, darker floor-standing
  unit, in practice often sited away from the scanner body."
  [0.40 0.43 0.48])

;; ---- parametric dimensions --------------------------------------------------

(defn unit->dimensions
  "Catalog unit -> illustrative component dimensions in METRES.

  Traceable to catalog values: `wafer-r` (from `:wafer-diameter-mm`),
  `stage-h` (`:stage-frontal-height-mm`) and `duct-h`
  (`:purge-duct-height-mm`) -- the three the CFD slice also consumes.
  Everything else is a proportion chosen to look plausible. See the ns
  docstring."
  [{:keys [wafer-diameter-mm stage-frontal-height-mm purge-duct-height-mm
           exposure-field-mm]
    :or {wafer-diameter-mm 300.0
         stage-frontal-height-mm 45.0
         purge-duct-height-mm 120.0
         exposure-field-mm [26.0 33.0]}}]
  (let [wafer-r (/ wafer-diameter-mm 2000.0)          ; mm dia -> m radius
        stage-h (/ stage-frontal-height-mm 1000.0)
        duct-h  (/ purge-duct-height-mm 1000.0)
        [field-x field-y] exposure-field-mm
        ;; the stage must travel a full wafer plus a field in each
        ;; direction, so its footprint is set by wafer + field stroke
        stage-x (+ (* 2.0 wafer-r) (/ field-y 500.0))
        stage-z (+ (* 2.0 wafer-r) (/ field-x 500.0))
        ;; base frame carries the stage plus metrology frame margin
        frame-x (* 2.6 stage-x)
        frame-z (* 2.2 stage-z)
        frame-h 0.85]
    {:wafer-r wafer-r
     :wafer-h 0.001                                    ; 300 mm wafers are ~775 um
     :stage [stage-x stage-h stage-z]
     :duct-h duct-h
     :frame [frame-x frame-h frame-z]
     :column-r (* 0.85 wafer-r)
     :column-h 1.45
     :hood-r (* 0.22 wafer-r)
     :hood-h (* 0.45 duct-h)
     :reticle [(* 1.5 stage-x) 0.22 (* 1.1 stage-z)]
     :illuminator [(* 1.1 stage-x) 0.55 (* 0.9 stage-z)]
     :plenum [(* 0.95 frame-x) 0.10 (* 0.9 frame-z)]
     :source [0.9 1.3 0.7]
     :load-port [0.42 0.55 0.42]}))

;; ---- instance constructors (mirror kami.webgpu.ir/instance's map shape) -----

(defn- box-instance
  "An instance map for a `:geo :box` primitive -- the field shape
  `kami.webgpu.ir/instance` returns, plus `:geo`."
  [color size pos & {:keys [yaw metallic roughness emissive]
                     :or {yaw 0 metallic 0.2 roughness 0.6 emissive 0.0}}]
  {:geo :box :pos pos :color color :size size :yaw yaw
   :metallic metallic :roughness roughness :emissive emissive})

(defn- cylinder-instance
  "An instance map for a `:geo :cylinder` primitive. `:size` follows
  `kami.webgpu.ir/instance-size`'s `[w h d]` convention with the
  footprint diameter in both `w`/`d` (a cylinder's plan-view bounding
  box is square)."
  [color r h pos & {:keys [yaw metallic roughness emissive]
                    :or {yaw 0 metallic 0.3 roughness 0.5 emissive 0.0}}]
  {:geo :cylinder :pos pos :color color :size [(* 2.0 r) h (* 2.0 r)] :yaw yaw
   :metallic metallic :roughness roughness :emissive emissive})

;; ---- the public scene builder ------------------------------------------------

(defn unit->render-ir
  "Catalog unit (e.g. `(printpressmfg.facts/unit-type-by-id
  :unit/arf-immersion-lithography-scanner)`) -> a kami.webgpu.ir v1
  render-IR map.

  Eleven instances, stacked along the optical axis in the order the
  light actually travels -- source cabinet, illuminator, reticle stage,
  projection optics column, immersion hood, wafer, wafer stage, base
  frame -- plus the purge plenum above the stage and two load ports at
  the front. Pure function; ground contact at y = 0."
  [unit]
  (let [{:keys [wafer-r wafer-h stage duct-h frame column-r column-h
                hood-r hood-h reticle illuminator plenum source load-port]}
        (unit->dimensions unit)
        [stage-x stage-h stage-z] stage
        [frame-x frame-h frame-z] frame
        [_ reticle-h _] reticle
        [_ illum-h _] illuminator
        [_ plenum-h _] plenum
        [source-w source-h source-d] source
        [lp-w lp-h lp-d] load-port

        frame-y   (/ frame-h 2.0)
        stage-y   (+ frame-h (/ stage-h 2.0))
        wafer-y   (+ frame-h stage-h (/ wafer-h 2.0))
        ;; the immersion hood sits in the gap directly above the wafer
        hood-y    (+ frame-h stage-h wafer-h (/ hood-h 2.0))
        column-y  (+ frame-h stage-h wafer-h hood-h (/ column-h 2.0))
        reticle-y (+ frame-h stage-h wafer-h hood-h column-h (/ reticle-h 2.0))
        illum-y   (+ frame-h stage-h wafer-h hood-h column-h reticle-h
                     (/ illum-h 2.0))
        ;; the plenum face is one duct-height above the stage TOP -- the
        ;; same gap printpressmfg.cfd turns into its channel
        plenum-y  (+ frame-h stage-h duct-h (/ plenum-h 2.0))
        source-x  (- (+ (/ frame-x 2.0) (/ source-w 2.0) 0.6))
        lp-z      (+ (/ frame-z 2.0) (/ lp-d 2.0) 0.05)
        lp-x      (* 0.30 frame-x)
        total-h   (+ illum-y (/ illum-h 2.0))]
    {:globals
     {:sky {:horizon [0.62 0.66 0.72] :sun-dir [0.35 0.85 0.4] :sun [1.0 0.99 0.95]}
      :eye [(+ frame-x 3.0) (+ total-h 1.2) (+ frame-z 3.4)]
      :target [0 (* 0.55 total-h) 0]}
     :instances
     [;; vibration-isolated base frame
      (box-instance frame-color frame [0 frame-y 0]
                    :metallic 0.15 :roughness 0.75)
      ;; wafer stage (the bluff body in the CFD model)
      (box-instance stage-color [stage-x stage-h stage-z] [0 stage-y 0]
                    :metallic 0.55 :roughness 0.35)
      ;; the 300 mm wafer itself -- specular polished silicon
      (cylinder-instance wafer-color wafer-r wafer-h [0 wafer-y 0]
                         :metallic 0.9 :roughness 0.08)
      ;; water-filled immersion hood between final lens and wafer
      (cylinder-instance immersion-color hood-r hood-h [0 hood-y 0]
                         :metallic 0.05 :roughness 0.12 :emissive 0.04)
      ;; projection optics column
      (cylinder-instance optics-color column-r column-h [0 column-y 0]
                         :metallic 0.4 :roughness 0.3)
      ;; reticle (mask) stage above the column
      (box-instance panel-color reticle [0 reticle-y 0]
                    :metallic 0.35 :roughness 0.45)
      ;; illuminator optics housing
      (box-instance optics-color illuminator [0 illum-y 0]
                    :metallic 0.3 :roughness 0.4)
      ;; purge-air plenum / laminar-flow face -- the CFD inflow boundary
      (box-instance plenum-color plenum [0 plenum-y 0]
                    :metallic 0.1 :roughness 0.85 :emissive 0.06)
      ;; ArF excimer laser source cabinet, floor-standing beside the body
      (box-instance source-color [source-w source-h source-d]
                    [source-x (/ source-h 2.0) 0]
                    :metallic 0.25 :roughness 0.55)
      ;; two FOUP load ports on the front face
      (box-instance panel-color load-port
                    [(- lp-x) (/ lp-h 2.0) lp-z]
                    :metallic 0.2 :roughness 0.6)
      (box-instance panel-color [lp-w lp-h lp-d]
                    [lp-x (/ lp-h 2.0) lp-z]
                    :metallic 0.2 :roughness 0.6)]}))

(defn instance-count
  "Number of primitives in the scene for `unit` -- convenience for tests
  and for callers sizing a draw batch."
  [unit]
  (count (:instances (unit->render-ir unit))))
