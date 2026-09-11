(ns etzhayyim.open-robo.urban-mining-core-test
  (:require [clojure.test :refer [deftest is testing]]
            [etzhayyim.open-robo.urban-mining-core :as umc]))

(deftest pick-stream-type-test
  (testing "label match wins first"
    (is (= "smartphone" (umc/pick-stream-type ["smartphone" "battery-visible"] {} umc/default-rules))))
  (testing "rare-earth XRF heuristic (nd / dy thresholds)"
    (is (= "rare-earth-magnet" (umc/pick-stream-type [] {"nd" 0.02} umc/default-rules)))
    (is (= "rare-earth-magnet" (umc/pick-stream-type [] {"dy" 0.003} umc/default-rules))))
  (testing "mixed-pcb XRF heuristic (cu / au_ppm thresholds)"
    (is (= "mixed-pcb" (umc/pick-stream-type [] {"cu" 0.09} umc/default-rules)))
    (is (= "mixed-pcb" (umc/pick-stream-type [] {"au_ppm" 60.0} umc/default-rules))))
  (testing "below thresholds -> unknown"
    (is (= "unknown" (umc/pick-stream-type [] {"cu" 0.01 "nd" 0.001} umc/default-rules))))
  (testing "absent/nil XRF keys coerce to 0.0 -> unknown"
    (is (= "unknown" (umc/pick-stream-type [] {} umc/default-rules)))))

(defn- r3 ^double [^double x] (/ (Math/round (* x 1000.0)) 1000.0))

(deftest score-confidence-test
  ;; compare at 3 dp: the raw float sum carries IEEE-754 drift (e.g.
  ;; 0.72 + 0.08 = 0.7999999999999999, identical to python), and the deployed
  ;; path rounds to 3 dp in classify-inspection anyway.
  (is (= 0.25 (r3 (umc/score-confidence "unknown" [] {}))))
  (testing "base only (no label, no xrf)"
    (is (= 0.72 (r3 (umc/score-confidence "smartphone" [] {})))))
  (testing "label-confirmed adds 0.17"
    (is (= 0.89 (r3 (umc/score-confidence "smartphone" ["smartphone"] {})))))
  (testing "xrf-present adds 0.08"
    (is (= 0.80 (r3 (umc/score-confidence "smartphone" [] {"cu" 0.1})))))
  (testing "both, capped at 0.99"
    (is (= 0.97 (r3 (umc/score-confidence "smartphone" ["smartphone"] {"cu" 0.1}))))))

(deftest classify-inspection-low-confidence-test
  (testing "unknown stream -> low confidence -> manual_review"
    (let [r (umc/classify-inspection {"item_id" "e1" "labels" [] "xrf" {} "mass_g" 120})]
      (is (= "unknown" (get r "stream_type")))
      (is (= "manual_review" (get r "destination_bin")))
      (is (= "manual_review_low_confidence" (get r "policy")))
      (is (= 0.25 (get r "confidence")))
      (is (= [] (get r "target_materials")))
      (is (= "e1" (get r "item_id")))
      (is (= 120 (get r "mass_g")))
      (is (= umc/lexicon (get r "toshi_kozan_lexicon"))))))

(deftest classify-inspection-hazard-test
  (testing "battery hazard forces li_ion_isolation even with a confident non-battery stream"
    (let [r (umc/classify-inspection {"item_id" "e2"
                                      "labels" ["smartphone" "swollen-battery"]
                                      "xrf" {"cu" 0.1}})]
      (is (= "smartphone" (get r "stream_type")))
      (is (= ["swollen-battery"] (get r "hazards")))
      (is (= "li_ion_isolation" (get r "destination_bin")))
      (is (= "isolate_battery_first" (get r "policy")))
      (is (= 0.97 (get r "confidence"))))))

(deftest classify-inspection-rule-test
  (testing "confident, non-hazard -> sort by material rule"
    (let [r (umc/classify-inspection {"item_id" "e3"
                                      "labels" ["server"]
                                      "xrf" {"au_ppm" 80.0}})]
      (is (= "server" (get r "stream_type")))
      (is (= [] (get r "hazards")))
      (is (= "mixed_pcb" (get r "destination_bin")))
      (is (= "sort_by_material_rule" (get r "policy")))
      (is (= ["au" "ag" "cu" "pd"] (get r "target_materials"))))))

(deftest hazards-sorted-deduped-test
  (testing "hazards are the sorted intersection with battery labels"
    (let [r (umc/classify-inspection {"labels" ["li-ion" "battery-visible" "pc"]})]
      (is (= ["battery-visible" "li-ion"] (get r "hazards"))))))

(deftest build-sort-command-test
  (testing "arm ready -> sort_commanded with target pose"
    (let [cls (umc/classify-inspection {"item_id" "e4" "labels" ["server"] "xrf" {"au_ppm" 80.0}})
          cmd (umc/build-sort-command cls)]
      (is (= "sort_commanded" (get cmd "event_type")))
      (is (= "mixed_pcb" (get cmd "destination_bin")))
      (is (= (umc/default-bin-targets "mixed_pcb") (get cmd "target_pose")))
      (is (= "e4" (get cmd "item_id")))))
  (testing "arm not ready -> sort_blocked"
    (let [cls (umc/classify-inspection {"item_id" "e5" "labels" ["server"] "xrf" {"au_ppm" 80.0}})
          cmd (umc/build-sort-command cls {:arm-status "homing"})]
      (is (= "sort_blocked" (get cmd "event_type")))
      (is (= "arm_not_ready" (get cmd "reason")))
      (is (= "homing" (get cmd "arm_status")))
      (is (= "hold_until_robot_ready" (get cmd "policy")))))
  (testing "unknown destination_bin falls back to manual_review"
    (let [cmd (umc/build-sort-command {"destination_bin" "no_such_bin" "item_id" "e6"})]
      (is (= "manual_review" (get cmd "destination_bin")))
      (is (= (umc/default-bin-targets "manual_review") (get cmd "target_pose"))))))

(deftest quaternion-from-euler-test
  (testing "identity rotation -> [0 0 0 1]"
    (is (= [0.0 0.0 0.0 1.0] (umc/quaternion-from-euler 0.0 0.0 0.0))))
  (testing "yaw pi/2 about z"
    (let [[x y z w] (umc/quaternion-from-euler 0.0 0.0 (/ Math/PI 2.0))
          tol 1e-9]
      (is (< (Math/abs (double x)) tol))
      (is (< (Math/abs (double y)) tol))
      (is (< (Math/abs (- (double z) (Math/sin (/ Math/PI 4.0)))) tol))
      (is (< (Math/abs (- (double w) (Math/cos (/ Math/PI 4.0)))) tol)))))

(deftest build-audit-event-test
  (testing "command fields take precedence, falling back to classification"
    (let [cls (umc/classify-inspection {"item_id" "e7" "labels" ["server"] "xrf" {"au_ppm" 80.0}})
          cmd (umc/build-sort-command cls)
          audit (umc/build-audit-event cls cmd)]
      (is (= "sort_commanded" (get audit "event_type")))
      (is (= "e7" (get audit "item_id")))
      (is (= "server" (get audit "stream_type")))
      (is (= "mixed_pcb" (get audit "destination_bin")))
      (is (= ["au" "ag" "cu" "pd"] (get audit "target_materials")))
      (is (= umc/lexicon (get audit "toshi_kozan_lexicon")))))
  (testing "empty command falls back to classification for shared fields"
    (let [cls {"item_id" "e8" "stream_type" "pc" "confidence" 0.89
               "hazards" [] "policy" "sort_by_material_rule" "target_materials" ["au"]}
          audit (umc/build-audit-event cls {})]
      (is (= "e8" (get audit "item_id")))
      (is (= "pc" (get audit "stream_type")))
      (is (= 0.89 (get audit "confidence")))
      (is (= "sort_by_material_rule" (get audit "policy"))))))
