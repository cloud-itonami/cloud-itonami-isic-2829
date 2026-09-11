(ns printpressmfg.facts-test
  (:require [clojure.test :refer [deftest is testing]]
            [printpressmfg.facts :as f]))

;; ----------------------------- GTIN check digit -----------------------------

(deftest gtin-check-digit-matches-gs1-worked-example
  ;; GS1's own published EAN-13 worked example: 400638133393 -> 1.
  ;; If this fails, every :gtin in the catalog is suspect.
  (is (= 1 (f/gtin13-check-digit "400638133393"))))

(deftest gtin-check-digit-matches-sibling-catalog-value
  ;; cloud-itonami-isic-2822's own catalog carries 0212822000018 for its
  ;; first entry -- same RCN(021)+ISIC+sequence construction. Recomputing
  ;; its check digit here proves this ns implements the SAME algorithm the
  ;; sibling catalogs used, not a lookalike.
  (is (= 8 (f/gtin13-check-digit "021282200001"))))

(deftest catalog-gtins-are-syntactically-valid
  (doseq [[id unit] f/unit-types]
    (testing (str id)
      (is (f/gtin13-valid? (:gtin unit))
          "catalog GTIN must carry a correct Modulo-10 check digit"))))

(deftest gtin-validity-rejects-a-corrupted-check-digit
  ;; Guard against `gtin13-valid?` being vacuously true.
  (is (false? (f/gtin13-valid? "0212829000010")))
  (is (false? (f/gtin13-valid? "021282900001")))
  (is (false? (f/gtin13-valid? nil))))

;; ----------------------------- catalog invariants -----------------------------

(deftest every-unit-declares-an-eight-digit-unspsc-code
  (doseq [[id unit] f/unit-types]
    (testing (str id)
      (is (= 8 (count (:unspsc-code unit))))
      (is (every? #(<= 0 (- (int %) 48) 9) (:unspsc-code unit))))))

(deftest every-gtin-is-flagged-as-an-unissued-placeholder
  ;; The catalog must never present a placeholder as a real GS1-issued
  ;; identifier -- the sibling key is what makes that machine-checkable.
  (doseq [[id unit] f/unit-types]
    (testing (str id)
      (is (= :unissued-blueprint-placeholder (:gtin/status unit))))))

(deftest gtin-encodes-the-rcn-prefix-and-this-actors-isic-code
  (doseq [[id unit] f/unit-types]
    (testing (str id)
      (is (= "021" (subs (:gtin unit) 0 3))
          "GS1 Restricted Circulation Number prefix")
      (is (= "2829" (subs (:gtin unit) 3 7))
          "this actor's own ISIC class"))))

(deftest catalog-entry-id-matches-its-key
  (doseq [[id unit] f/unit-types]
    (testing (str id)
      (is (= id (:id unit))))))

(deftest unit-type-lookup-round-trips
  (is (= (f/unit-type-by-id :unit/arf-immersion-lithography-scanner)
         (get f/unit-types :unit/arf-immersion-lithography-scanner)))
  (is (nil? (f/unit-type-by-id :unit/not-a-real-unit)))
  (is (= (vec (sort (keys f/unit-types))) (f/unit-type-ids))))

;; ----------------------------- optical relations -----------------------------

(def scanner (f/unit-type-by-id :unit/arf-immersion-lithography-scanner))

(deftest numerical-aperture-stays-below-the-immersion-medium-index
  ;; The physical ceiling: NA < n. An NA at or above the medium's
  ;; refractive index would be unphysical, so this guards the catalog
  ;; against a plausible-looking but impossible edit.
  (is (< (:numerical-aperture scanner) (f/max-numerical-aperture scanner))))

(deftest rayleigh-resolution-is-computed-not-asserted
  ;; CD = k1 * lambda / NA. At the production-realistic k1 = 0.28 with
  ;; 193 nm / NA 1.35 this lands at ~40 nm, the well-known single-exposure
  ;; half-pitch for this tool generation.
  (let [cd (f/resolution-nm scanner 0.28)]
    (is (< 39.0 cd 41.0)))
  ;; k1 = 0.25 is the hard single-exposure physical floor -> ~36 nm.
  (let [cd-floor (f/resolution-nm scanner 0.25)]
    (is (< 35.0 cd-floor 37.0))
    (is (< cd-floor (f/resolution-nm scanner 0.28))
        "a smaller k1 must resolve finer")))

(deftest depth-of-focus-falls-quadratically-with-na
  ;; DOF = k2 * lambda / NA^2. Halving NA must quadruple DOF -- the
  ;; resolution/focus-budget trade that makes stage flatness and
  ;; environmental stability matter at high NA.
  (let [dof (f/depth-of-focus-nm scanner 1.0)
        dof-half-na (f/depth-of-focus-nm (assoc scanner :numerical-aperture
                                                (/ (:numerical-aperture scanner) 2.0))
                                         1.0)]
    (is (< (Math/abs (- (* 4.0 dof) dof-half-na)) 1e-6))))

(deftest exposure-field-is-the-semi-standard-size
  (is (= [26.0 33.0] (:exposure-field-mm scanner))))
