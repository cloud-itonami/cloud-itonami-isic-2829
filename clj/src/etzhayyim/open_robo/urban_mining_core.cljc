(ns etzhayyim.open-robo.urban-mining-core
  "Urban-mining inspection classifier — faithful clj/cljc port of
  `firmware/armcrawler/ros2/armcrawler_ros2/urban_mining_core.py`
  (ADR-2606280030, wave-2/3 python→clj migration).

  This is the ONLY pure-logic module in the open-robo firmware tree; every
  other python module is hardware/ROS2-bound (rclpy / RPi.GPIO / smbus2 /
  picamera2 / pyserial) or numpy/scipy numerics, which have no babashka
  equivalent and are kept as `.py` (see clj/README.md).

  Wire contract is preserved byte-for-byte: the ROS2 classifier/sorter nodes
  pass JSON-decoded maps (string keys) on `std_msgs/String`, so this port keeps
  STRING keys throughout — a `classify-inspection` result here serialises to the
  same JSON the python emits. The python module stays in place and remains the
  deployed code on the robot (Raspberry Pi 5 + ROS2 Humble); this namespace is
  the kotoba-Datom-log-native twin for off-robot reasoning over the
  `com.etzhayyim.apps.toshiKozan.registerEwasteStream` lexicon."
  (:require [clojure.set :as set]))

(def default-rules
  "Per stream-type sort policy (mirror of python DEFAULT_RULES)."
  {"smartphone"        {"destination_bin" "li_ion_isolation"  "target_materials" ["au" "ag" "cu" "li" "co"]}
   "pc"                {"destination_bin" "mixed_pcb"          "target_materials" ["au" "ag" "cu"]}
   "server"            {"destination_bin" "mixed_pcb"          "target_materials" ["au" "ag" "cu" "pd"]}
   "battery-li-ion"    {"destination_bin" "li_ion_isolation"  "target_materials" ["li" "co" "ni" "cu"]}
   "mixed-pcb"         {"destination_bin" "mixed_pcb"          "target_materials" ["au" "ag" "cu" "pd"]}
   "rare-earth-magnet" {"destination_bin" "rare_earth_magnet" "target_materials" ["nd" "dy"]}})

(def ^:private half-pi (/ Math/PI 2.0))

(def default-bin-targets
  "Per-bin arm target pose [x y z roll pitch yaw] (mirror of DEFAULT_BIN_TARGETS)."
  {"manual_review"     [3.95 0.52 0.42 0.0 0.0 half-pi]
   "li_ion_isolation"  [3.95 0.92 0.42 0.0 0.0 half-pi]
   "mixed_pcb"         [3.95 1.34 0.42 0.0 0.0 half-pi]
   "copper_aluminum"   [3.95 1.76 0.42 0.0 0.0 half-pi]
   "ferrous"           [3.95 2.18 0.42 0.0 0.0 half-pi]
   "rare_earth_magnet" [3.95 2.60 0.42 0.0 0.0 half-pi]
   "reject"            [3.95 2.88 0.42 0.0 0.0 half-pi]})

(def default-battery-labels
  "Visual hazard labels that force li-ion isolation."
  #{"battery-visible" "swollen-battery" "li-ion"})

(def default-low-confidence-threshold 0.68)

(def lexicon
  "Kotoba lexicon every classification/audit event is bound to."
  "com.etzhayyim.apps.toshiKozan.registerEwasteStream")

(defn- xrf-num
  "Mirror of python `float(xrf.get(k, 0.0) or 0.0)` — nil/absent -> 0.0."
  [xrf k]
  (double (or (get xrf k) 0.0)))

(defn- round3
  "Mirror of python `round(x, 3)` (no half-way cases occur for the fixed
  confidence values, so a simple scale/round/unscale is exact here)."
  [^double x]
  (/ (Math/round (* x 1000.0)) 1000.0))

(defn pick-stream-type
  "Pick the e-waste stream type from labels first, then XRF heuristics, else
  \"unknown\". Faithful to python `pick_stream_type`."
  [labels xrf rules]
  (or (some #(when (contains? rules %) %) labels)
      (cond
        (or (> (xrf-num xrf "nd") 0.01) (> (xrf-num xrf "dy") 0.002)) "rare-earth-magnet"
        (or (> (xrf-num xrf "cu") 0.08) (> (xrf-num xrf "au_ppm") 50.0)) "mixed-pcb"
        :else "unknown")))

(defn score-confidence
  "Confidence in the chosen stream type. Faithful to python `score_confidence`."
  [stream-type labels xrf]
  (if (= stream-type "unknown")
    0.25
    (let [base 0.72
          score (cond-> base
                  (some #(= stream-type %) labels) (+ 0.17)
                  (seq xrf) (+ 0.08))]
      (min score 0.99))))

(defn classify-inspection
  "Classify one inspection map into a sort decision. Faithful to python
  `classify_inspection`. `inspection` uses string keys (JSON wire contract).

  Optional opts: :rules :battery-labels :low-confidence-threshold."
  ([inspection] (classify-inspection inspection {}))
  ([inspection {:keys [rules battery-labels low-confidence-threshold]
                :or   {rules default-rules
                       battery-labels default-battery-labels
                       low-confidence-threshold default-low-confidence-threshold}}]
   (let [labels (mapv str (get inspection "labels" []))
         xrf (or (get inspection "xrf") {})
         stream-type (pick-stream-type labels xrf rules)
         confidence (score-confidence stream-type labels xrf)
         hazards (vec (sort (set/intersection (set labels) battery-labels)))
         [destination-bin policy]
         (cond
           (< confidence low-confidence-threshold) ["manual_review" "manual_review_low_confidence"]
           (seq hazards) ["li_ion_isolation" "isolate_battery_first"]
           :else [(get-in rules [stream-type "destination_bin"] "manual_review")
                  "sort_by_material_rule"])]
     {"item_id"             (get inspection "item_id")
      "stream_type"         stream-type
      "target_materials"    (get-in rules [stream-type "target_materials"] [])
      "destination_bin"     destination-bin
      "confidence"          (round3 confidence)
      "hazards"             hazards
      "policy"              policy
      "mass_g"              (get inspection "mass_g")
      "toshi_kozan_lexicon" lexicon})))

(defn build-sort-command
  "Build a sort command (or a blocked event) from a classification. Faithful to
  python `build_sort_command`."
  ([classification] (build-sort-command classification {}))
  ([classification {:keys [bin-targets arm-status arm-ready-status]
                    :or   {bin-targets default-bin-targets
                           arm-status "ready"
                           arm-ready-status "ready"}}]
   (let [destination0 (str (or (get classification "destination_bin") "manual_review"))
         destination (if (contains? bin-targets destination0) destination0 "manual_review")]
     (if (not= arm-status arm-ready-status)
       {"event_type"      "sort_blocked"
        "item_id"         (get classification "item_id")
        "reason"          "arm_not_ready"
        "arm_status"      arm-status
        "destination_bin" destination
        "policy"          "hold_until_robot_ready"}
       {"event_type"      "sort_commanded"
        "item_id"         (get classification "item_id")
        "stream_type"     (get classification "stream_type")
        "destination_bin" destination
        "target_pose"     (vec (get bin-targets destination))
        "confidence"      (get classification "confidence")
        "hazards"         (get classification "hazards" [])
        "policy"          (get classification "policy")}))))

(defn quaternion-from-euler
  "ZYX Tait-Bryan euler -> quaternion [x y z w]. Faithful to python
  `quaternion_from_euler`."
  [roll pitch yaw]
  (let [cy (Math/cos (* yaw 0.5))
        sy (Math/sin (* yaw 0.5))
        cp (Math/cos (* pitch 0.5))
        sp (Math/sin (* pitch 0.5))
        cr (Math/cos (* roll 0.5))
        sr (Math/sin (* roll 0.5))]
    [(- (* sr cp cy) (* cr sp sy))
     (+ (* cr sp cy) (* sr cp sy))
     (- (* cr cp sy) (* sr sp cy))
     (+ (* cr cp cy) (* sr sp sy))]))

(defn build-audit-event
  "Merge a classification + its command into an append-only audit event bound to
  the toshiKozan lexicon. Faithful to python `build_audit_event` (`or` fallbacks
  prefer the command field, falling back to the classification)."
  [classification command]
  {"event_type"          (get command "event_type")
   "item_id"             (or (get command "item_id") (get classification "item_id"))
   "stream_type"         (or (get command "stream_type") (get classification "stream_type"))
   "destination_bin"     (get command "destination_bin")
   "target_materials"    (get classification "target_materials" [])
   "confidence"          (or (get command "confidence") (get classification "confidence"))
   "hazards"             (or (get command "hazards") (get classification "hazards" []))
   "policy"              (or (get command "policy") (get classification "policy"))
   "toshi_kozan_lexicon" lexicon})
