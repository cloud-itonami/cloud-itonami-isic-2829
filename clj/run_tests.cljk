#!/usr/bin/env bb
;; Run the open-robo clj test suite under babashka (repo rule: run_tests.clj, not .sh).
;;   bb run_tests.clj    (from 60-apps/etzhayyim-project-open-robo/clj/)
(require '[clojure.test :as t]
         '[etzhayyim.open-robo.urban-mining-core-test])

(let [{:keys [fail error]} (t/run-tests 'etzhayyim.open-robo.urban-mining-core-test)]
  (System/exit (if (pos? (+ (or fail 0) (or error 0))) 1 0)))
