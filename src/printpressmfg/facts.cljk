(ns printpressmfg.facts
  "Catalog of concrete manufactured UNIT MODELS this ISIC 2829 actor's
  units can declare a `:unit-type-id` reference to.

  Same shape and same conventions as `machinetool.facts/unit-types`
  (cloud-itonami-isic-2822) and `pressureequip.facts/unit-types`
  (cloud-itonami-isic-2813) -- no shared code, this actor's own
  independent copy of the same catalog convention, per this fleet's
  standing 'do not invent a new shared shape' discipline.

  SCOPE NOTE -- why a lithography scanner lives in THIS repo. ISIC 2829
  (Manufacture of other special-purpose machinery) is a RESIDUAL n.e.c.
  class whose published inclusion list covers BOTH printing machinery
  AND machinery for making semiconductor devices. This repo's advisor/
  governor slice picked ONE illustrative product line (industrial
  printing presses -- hence the historical `printpressmfg` namespace
  prefix, which names the repo's first product line, NOT the boundary
  of its ISIC class). A semiconductor lithography scanner is a second
  product line inside the SAME ISIC class, so it is added here as a
  `unit-types` entry rather than as a new repo -- exactly the judgement
  ADR-2800000400 recorded when it put a metal additive-manufacturing
  system into the existing isic-2822 catalog instead of standing up a
  dedicated repo.

  This namespace is pure data + pure functions. It does NOT touch the
  governor, the operation allowlist, or any actuation path -- adding a
  catalog entry never widens what this actor is permitted to do.

  ## Classification fields come from EXTERNAL authorities, reported honestly

  `:unspsc-code` -- 8-digit UNSPSC (United Nations Standard Products
  and Services Code). The hierarchy used here was confirmed against TWO
  INDEPENDENT public sources, per the anti-fabrication discipline this
  fleet applies to every classification code:

    segment  23000000  Industrial Manufacturing and Processing
                       Machinery and Accessories
    family   23210000  Electronic manufacturing machinery and
                       equipment and accessories
    class    23211100  Electronic manufacturing and processing
                       machinery
    commodity 23211101 Semiconductor process systems

  Source (1): the NASA SEWP official UNSPSC scope PDF
  (sewp.nasa.gov/documents/SEWP_UNSPSC_Codes.pdf) lists the
  segment/family/class chain 23000000 -> 23210000 -> 23211100 verbatim,
  including its sibling class 23211000 'Electronic assembly machinery
  and support equipment'. Source (2): the usa.databasesets.com class
  page for 23211100 enumerates all SIX commodities under that class
  (23211101 Semiconductor process systems / 23211102 Printed circuit
  board making system / 23211103 Wafer wire bonder / 23211104
  Semiconductor chip inspection monitor / 23211105 Vacuum impregnation
  or porosity sealing device / 23211106 Ion implanter), and its family
  page for 23210000 confirms the same two-class structure.

  HONEST INTERPRETATION, stated plainly: **UNSPSC publishes no
  lithography-, stepper-, or scanner-specific commodity code.** The six
  commodities above are the complete published set for this class, and
  none of them names photolithography. `23211101` ('Semiconductor
  process systems') is the closest published commodity -- a
  lithography scanner IS a semiconductor process system, and it is the
  only entry in the class broad enough to contain one -- so it is used
  here as a documented INTERPRETATION, not as an official UNSPSC
  lithography code. This namespace does not fabricate a more specific
  commodity code that UNSPSC does not publish.

  `:gtin` -- a GTIN is NOT a classification taxonomy code at all; it is
  an identifier GS1 issues per REGISTERED PHYSICAL PRODUCT, only after
  a real company enrolls with GS1 and assigns it. Like the isic-2822/
  isic-2813 catalogs, the `:gtin` here is a SYNTACTICALLY VALID but
  NEVER-ISSUED placeholder: GS1's own documented 'Restricted
  Circulation Number' (RCN) prefix `021` (the 020-029 reserved-for-
  internal-use range), then this actor's ISIC code (2829), then a
  sequence, then a correctly computed Modulo-10 GTIN-13 check digit
  (algorithm verified against the standard EAN-13 worked example
  400638133393 -> 1, the same worked example the sibling catalogs
  cite). The sibling key `:gtin/status
  :unissued-blueprint-placeholder` makes the non-issuance explicit and
  machine-checkable. Treat `:gtin` as an EXAMPLE VALUE ONLY.

  ## Which physical parameters are real, and which are illustrative

  Some values below are GENUINE, publicly documented properties of the
  ArF-immersion lithography generation, and are marked as such:
  `:exposure-wavelength-nm 193.0` (the ArF excimer laser line),
  `:immersion-medium-index 1.44` (ultrapure water at 193 nm),
  `:numerical-aperture 1.35` (the industry-standard ceiling for
  water-immersion ArF -- NA is bounded above by the immersion medium's
  refractive index), `:exposure-field-mm [26.0 33.0]` (the SEMI
  standard maximum exposure field), and `:wafer-diameter-mm 300.0`.

  Everything else (`:stage-scan-speed-mm-s`, `:throughput-wph`,
  `:overlay-budget-nm`, `:purge-face-velocity-m-s`,
  `:stage-frontal-height-mm`, `:purge-duct-height-mm`) is an
  ILLUSTRATIVE value in a plausible range for this equipment class. It
  is NOT taken from any manufacturer's specification sheet, and must
  not be cited as one."
  )

;; ---------------------------------------------------------------------------
;; GTIN-13 Modulo-10 check digit (pure; used by the catalog's own tests)
;; ---------------------------------------------------------------------------

(defn gtin13-check-digit
  "The GS1 Modulo-10 check digit for a 12-digit GTIN-13 payload string.

  Weights alternate 1,3,1,3,... from the LEFT across the 12 payload
  digits; the check digit is the amount needed to round the weighted
  sum up to the next multiple of 10. Verified against GS1's own
  published worked example: payload `400638133393` -> check digit 1."
  [payload]
  (let [ds (mapv #?(:clj  #(Character/digit ^char % 10)
                    :cljs #(js/parseInt % 10))
                 payload)
        sum (reduce + (map-indexed (fn [i d] (* d (if (even? i) 1 3))) ds))]
    (mod (- 10 (mod sum 10)) 10)))

(defn gtin13-valid?
  "True when `gtin` is a 13-character all-digit string whose final digit
  is the correct Modulo-10 check digit for its first 12."
  [gtin]
  (boolean
   (and (string? gtin)
        (= 13 (count gtin))
        (every? #(<= 0 (int (- #?(:clj (int %) :cljs (.charCodeAt % 0)) 48)) 9) gtin)
        (= (gtin13-check-digit (subs gtin 0 12))
           #?(:clj  (Character/digit ^char (nth gtin 12) 10)
              :cljs (js/parseInt (nth gtin 12) 10))))))

;; ---------------------------------------------------------------------------
;; Optical design relations (genuine first-principles physics, not lookups)
;; ---------------------------------------------------------------------------

(defn resolution-nm
  "Rayleigh resolution (half-pitch) of a projection lithography system:

      CD = k1 * lambda / NA

  `k1` is the process-difficulty factor (dimensionless): ~0.25 is the
  hard single-exposure physical floor, ~0.28-0.35 is a realistic
  production single-exposure window, and values below 0.25 are only
  reachable with multiple patterning (which this single-exposure
  relation does NOT model). This is the textbook Rayleigh criterion,
  computed here rather than asserted as a spec number."
  [{:keys [exposure-wavelength-nm numerical-aperture]} k1]
  (/ (* k1 exposure-wavelength-nm) numerical-aperture))

(defn depth-of-focus-nm
  "Rayleigh depth of focus of a projection lithography system:

      DOF = k2 * lambda / NA^2

  `k2` is a process factor of order 1. The NA^2 denominator is the
  reason high-NA immersion tools trade focus budget for resolution --
  the same NA increase that shrinks CD linearly shrinks DOF
  quadratically, which is precisely why wafer-stage flatness, focus
  control and the thermal/flow stability this unit's CFD slice probes
  matter at this NA."
  [{:keys [exposure-wavelength-nm numerical-aperture]} k2]
  (/ (* k2 exposure-wavelength-nm) (* numerical-aperture numerical-aperture)))

(defn max-numerical-aperture
  "The physical ceiling on NA for a given immersion medium: NA < n,
  where `n` is the medium's refractive index at the exposure
  wavelength. Water (n ~ 1.44 at 193 nm) is why production ArF
  immersion tools top out at NA 1.35 -- the residual margin below 1.44
  is consumed by finite lens aperture and angular-acceptance limits."
  [{:keys [immersion-medium-index]}]
  immersion-medium-index)

;; ---------------------------------------------------------------------------
;; The catalog
;; ---------------------------------------------------------------------------

(def unit-types
  "Manufactured unit models this actor's plant produces. See the ns
  docstring for the UNSPSC/GTIN provenance and the real-vs-illustrative
  parameter split."
  {:unit/arf-immersion-lithography-scanner
   {:id :unit/arf-immersion-lithography-scanner
    :name "ArF液浸半導体露光装置(スキャナ)"
    :product-line :semiconductor-lithography

    ;; --- genuine, publicly documented properties of this tool class ---
    ;; ArF excimer laser line.
    :exposure-wavelength-nm 193.0
    ;; Ultrapure water at 193 nm. Bounds NA from above (see
    ;; `max-numerical-aperture`).
    :immersion-medium "ultrapure water"
    :immersion-medium-index 1.44
    ;; Industry-standard ceiling for water-immersion ArF.
    :numerical-aperture 1.35
    ;; SEMI standard maximum exposure field, in mm.
    :exposure-field-mm [26.0 33.0]
    :wafer-diameter-mm 300.0

    ;; --- illustrative values (plausible range, NOT vendor data) ---
    :stage-scan-speed-mm-s 600.0
    :throughput-wph 275
    :overlay-budget-nm 2.0
    ;; Environmental purge over the wafer stage. The scanner's optical
    ;; metrology path runs through this gas, so its uniformity is an
    ;; overlay-budget term, not merely a contamination-control term --
    ;; this is what `printpressmfg.cfd` models.
    :purge-gas :clean-dry-air
    :purge-face-velocity-m-s 0.45
    :purge-duct-height-mm 120.0
    :stage-frontal-height-mm 45.0

    ;; --- classification (see ns docstring for provenance) ---
    :unspsc-code "23211101"
    ;; Single-line on purpose: catalog data must not carry source
    ;; indentation into whatever consumes it.
    :unspsc/interpretation
    (str "UNSPSC publishes no lithography-specific commodity; 23211101 "
         "'Semiconductor process systems' is the closest published commodity "
         "in class 23211100 and is used as a documented interpretation.")
    :gtin "0212829000011"
    :gtin/status :unissued-blueprint-placeholder}})

(defn unit-type-by-id
  "The catalog entry for `id`, or nil."
  [id]
  (get unit-types id))

(defn unit-type-ids
  "All catalog `:unit-type-id` values, sorted for deterministic output."
  []
  (vec (sort (keys unit-types))))
