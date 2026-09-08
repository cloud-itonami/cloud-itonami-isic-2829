(ns printpressmfg.render-html
  "Build-time HTML renderer for `docs/samples/operator-console.html`.

  Closes flagship checklist item 2 for this repo: before this namespace
  existed there was NO generator and NO sample console here at all.

  This drives the REAL actor stack -- `printpressmfg.operation`
  (a compiled langgraph-clj StateGraph) -> `printpressmfg.governor`
  -> `printpressmfg.phase` -> `printpressmfg.store` -- through
  `langgraph.graph/run*`, exactly as `printpressmfg.sim` does, and
  renders the resulting store + audit ledger. EVERY value on the page
  is real governor/store output against this repo's own seeded
  `printpressmfg.store/sample-data!` entities (`batch-001`..`batch-003`,
  `fab-001`, `bench-002`). Nothing is hand-typed, invented or copied
  from a previous run.

  Why `.clj` and not `.cljc` (the repo's default): this is build
  tooling, not the actor. It runs once on the JVM at build time to
  produce a static artifact; it is never on the actuation path and is
  never loaded by the governor. Same split the sibling reference
  implementation `cloud-itonami-isic-9522`'s
  `applianceshop.render-html` uses.

  ## Scenario coverage

  The scenario in `run-demo!` is broader than `printpressmfg.sim`: it
  exercises ALL TWELVE of `printpressmfg.governor`'s rules (sim covers
  eleven, and never isolates `:equipment-control-blocked` from
  `:unknown-op`), plus the two HOLD sources that are NOT the governor:
  the `printpressmfg.phase` rollout gate (`:phase-disabled`) and a
  human approver's rejection (`:approver-rejected`).

  ## Determinism

  No timestamps, no randomness, no wall-clock, no locale-dependent
  number formatting (`fmt2` pins `Locale/ROOT`). All collections are
  rendered in a fixed order -- store accessors that back a table are
  either already `sort-by :id` (`all-batches` / `all-equipment` /
  `all-maintenance`) or append-ordered logs (`ledger`,
  `safety-concerns`, `*-history`); every set is sorted before it is
  printed. Two consecutive runs are byte-identical.

  ## Build-time invariant

  `-main` THROWS if the run produced no `:governor-hold` record, or
  fewer than `min-distinct-hold-rules` distinct hold rules. A console
  that quietly renders zero holds would claim a governed actor while
  showing an ungoverned one; that is a build failure here, not a
  convention.

  Usage: `clojure -M:dev:render-html [out-file]`
  (default `docs/samples/operator-console.html`)."
  (:require [kotoba.lang.text :as str]
            [langgraph.graph :as g]
            [printpressmfg.advisor :as advisor]
            [printpressmfg.facts :as facts]
            [printpressmfg.governor :as governor]
            [printpressmfg.operation :as op]
            [printpressmfg.phase :as phase]
            [printpressmfg.store :as store]))

;; ---------------------------------------------------------------------------
;; scenario
;; ---------------------------------------------------------------------------

(def ^:private coordinator
  "The real request context every sibling demo uses -- phase 3
  (`supervised-auto`), the highest phase this actor has."
  {:actor-id "coord-1" :actor-role :plant-coordinator :phase 3})

(def ^:private read-only-coordinator
  "Same coordinator, pinned to phase 0 (`read-only`) -- used once, to
  show that the rollout phase gate can HOLD a proposal the governor
  itself cleared."
  {:actor-id "coord-1" :actor-role :plant-coordinator :phase 0})

(def min-distinct-hold-rules
  "Build-time floor on hold-rule variety. `printpressmfg.governor`
  defines twelve rules; `printpressmfg.phase` and the approval path add
  `:phase-disabled` and `:approver-rejected`. Anything below this means
  the scenario stopped exercising the compliance layer."
  12)

(defn- exec! [actor tid request context]
  (g/run* actor {:request request :context context} {:thread-id tid}))

(defn- approve! [actor tid]
  (g/run* actor {:approval {:status :approved :by "coord-1"}}
          {:thread-id tid :resume? true}))

(defn- reject! [actor tid]
  (g/run* actor {:approval {:status :rejected :by "coord-1"}}
          {:thread-id tid :resume? true}))

(defn- compromised-advisor
  "A deliberately COMPROMISED `printpressmfg.advisor/Advisor`: it runs
  the repo's own real `advisor/infer` and then swaps ONLY the declared
  `:effect` for a direct fabrication-line actuation.

  This is the exact threat `printpressmfg.advisor`'s own docstring
  names ('a mis-wired caller can never reach a commit path even if this
  advisor were compromised') and the only way to observe the governor's
  `:equipment-control-blocked` rule ISOLATED -- with the honest mock
  advisor that rule only ever co-fires with `:unknown-op`, because the
  mock's effect is hard-wired per op.

  Nothing else is faked: the store, the graph, the governor, the phase
  gate and the ledger are all the real ones."
  [rogue-effect]
  (reify advisor/Advisor
    (-advise [_ st req]
      (assoc (advisor/infer st req) :effect rogue-effect))))

(defn run-demo!
  "Runs one freshly seeded `printpressmfg.store` through a scenario that
  reaches every disposition this actor can produce.

  COMMITTED (each one governor-clean, then phase-gated):
    - `batch-001` production-batch log -- phase-3 AUTO-commit (the only
      op in any phase's `:auto` set).
    - `mnt-1` maintenance window on `fab-001` (verified + registered
      fabrication line) -- escalates, human approves, commits
      `MNT-000000`.
    - `concern-1` safety concern on `fab-001` -- ALWAYS escalates
      (`:coordination/safety-concern` is permanently high-stakes),
      human approves, commits.
    - `ship-1` shipment of 10 units of `batch-001` -- escalates, human
      approves, commits `SHP-000000` (8 shipped + 10 = 18 of 25).
    - `ship-2` shipment of 7 more units of `batch-001` -- fills the
      batch EXACTLY to its recorded 25-unit capacity. Legal, and the
      reason `registry/shipment-quantity-exceeded?` compares at
      1/10000 of a unit instead of on raw doubles.

  HELD BY A HUMAN (not the governor):
    - `mnt-9` on `fab-001` -- governor-clean, escalates, and the human
      approver REJECTS it: `:approver-rejected`.

  HELD BY THE ROLLOUT PHASE (not the governor):
    - `batch-001` logged by a phase-0 (`read-only`) coordinator --
      governor-clean, `:phase-disabled`.

  HARD-HELD BY THE GOVERNOR (never reaches a human, no override) --
  all twelve rules:
    1  `:not-propose-effect`             request `:effect :direct-write`
    2  `:unknown-op`                     op `:actuate-fabrication-line`
    3  `:equipment-control-blocked`      isolated, via `compromised-advisor`
    4  `:equipment-actuate-blocked`      `mnt-3` `:actuate-equipment? true`
    5  `:certification-authority-blocked` on `:log-production-batch`
       and again on `:schedule-maintenance` (`mnt-5`) -- the rule is
       op-independent, and this shows it
    6  `:equipment-not-verified`         `mnt-2` on UNVERIFIED `bench-002`
    7  `:already-scheduled`              `mnt-1` a second time
    8  `:batch-not-verified`             `ship-3` on UNVERIFIED `batch-003`
    9  `:shipment-quantity-exceeded`     `ship-4`: 14 shipped + 5 > 15
       recorded for `batch-002`; and `ship-5`, which states NO quantity
       at all -- un-checkable headroom is not headroom
    10 `:invalid-product-type`           `:unobtainium`
    11 `:invalid-registration-accuracy`  999.0 mm
    12 `:invalid-defect-rate`            999.0 %

  Returns the store. Every field the page reads comes from here."
  []
  (let [db (-> (store/mem-store) (store/sample-data!))
        actor (op/build db)
        rogue (op/build db {:advisor (compromised-advisor :fabrication-line/actuate)})]

    ;; ---- committed lane -------------------------------------------------
    (exec! actor "c1" {:op :log-production-batch :effect :propose :subject "batch-001"
                       :patch {:product-type :offset-press :last-assessed "2026-07-15"}}
           coordinator)

    (exec! actor "c2" {:op :schedule-maintenance :effect :propose :subject "mnt-1"
                       :value {:equipment-id "fab-001" :maintenance-type :feeder-inspection
                               :scheduled-date "2026-08-01" :actuate-equipment? false}}
           coordinator)
    (approve! actor "c2")

    (exec! actor "c3" {:op :flag-safety-concern :effect :propose :subject "concern-1"
                       :value {:equipment-id "fab-001" :severity :moderate
                               :description "印刷胴のニップ点付近の異音、巻き込みリスク兆候"}}
           coordinator)
    (approve! actor "c3")

    (exec! actor "c4" {:op :coordinate-shipment :effect :propose :subject "ship-1"
                       :value {:batch-id "batch-001" :units 10.0
                               :destination "buyer-print-shop-north"}}
           coordinator)
    (approve! actor "c4")

    (exec! actor "c5" {:op :coordinate-shipment :effect :propose :subject "ship-2"
                       :value {:batch-id "batch-001" :units 7.0
                               :destination "buyer-print-shop-west"}}
           coordinator)
    (approve! actor "c5")

    ;; ---- human rejection -------------------------------------------------
    (exec! actor "r1" {:op :schedule-maintenance :effect :propose :subject "mnt-9"
                       :value {:equipment-id "fab-001" :maintenance-type :ink-train-service
                               :scheduled-date "2026-10-01" :actuate-equipment? false}}
           coordinator)
    (reject! actor "r1")

    ;; ---- rollout-phase hold (governor-clean) -----------------------------
    (exec! actor "p1" {:op :log-production-batch :effect :propose :subject "batch-001"
                       :patch {:product-type :offset-press}}
           read-only-coordinator)

    ;; ---- HARD governor holds ---------------------------------------------
    (exec! actor "h1" {:op :log-production-batch :effect :direct-write :subject "batch-001"
                       :patch {:product-type :offset-press}}
           coordinator)

    (exec! actor "h2" {:op :actuate-fabrication-line :effect :propose :subject "fab-001"}
           coordinator)

    (exec! rogue "h3" {:op :log-production-batch :effect :propose :subject "batch-001"
                       :patch {:product-type :offset-press}}
           coordinator)

    (exec! actor "h4" {:op :schedule-maintenance :effect :propose :subject "mnt-3"
                       :value {:equipment-id "fab-001" :maintenance-type :force-run
                               :scheduled-date "2026-09-01" :actuate-equipment? true}}
           coordinator)

    (exec! actor "h5" {:op :log-production-batch :effect :propose :subject "batch-001"
                       :patch {:issue-certification? true}}
           coordinator)

    (exec! actor "h6" {:op :schedule-maintenance :effect :propose :subject "mnt-5"
                       :value {:equipment-id "fab-001" :maintenance-type :ce-marking
                               :scheduled-date "2026-09-15" :actuate-equipment? false
                               :issue-certification? true}}
           coordinator)

    (exec! actor "h7" {:op :schedule-maintenance :effect :propose :subject "mnt-2"
                       :value {:equipment-id "bench-002" :maintenance-type :calibration
                               :scheduled-date "2026-08-01" :actuate-equipment? false}}
           coordinator)

    (exec! actor "h8" {:op :schedule-maintenance :effect :propose :subject "mnt-1"
                       :value {:equipment-id "fab-001" :maintenance-type :feeder-inspection
                               :scheduled-date "2026-08-01" :actuate-equipment? false}}
           coordinator)

    (exec! actor "h9" {:op :coordinate-shipment :effect :propose :subject "ship-3"
                       :value {:batch-id "batch-003" :units 1.0
                               :destination "buyer-print-shop-south"}}
           coordinator)

    (exec! actor "h10" {:op :coordinate-shipment :effect :propose :subject "ship-4"
                        :value {:batch-id "batch-002" :units 5.0
                                :destination "buyer-print-shop-east"}}
           coordinator)

    (exec! actor "h11" {:op :coordinate-shipment :effect :propose :subject "ship-5"
                        :value {:batch-id "batch-001"
                                :destination "buyer-print-shop-central"}}
           coordinator)

    (exec! actor "h12" {:op :log-production-batch :effect :propose :subject "batch-001"
                        :patch {:product-type :unobtainium}}
           coordinator)

    (exec! actor "h13" {:op :log-production-batch :effect :propose :subject "batch-001"
                        :patch {:registration-accuracy-mm 999.0}}
           coordinator)

    (exec! actor "h14" {:op :log-production-batch :effect :propose :subject "batch-001"
                        :patch {:defect-rate-percent 999.0}}
           coordinator)
    db))

;; ---------------------------------------------------------------------------
;; derived views over the real run
;; ---------------------------------------------------------------------------

(defn hold-facts
  "Every HOLD fact the run wrote to the append-only ledger -- governor
  HARD holds, phase-gate holds, and approver rejections."
  [ledger]
  (filterv #(#{:governor-hold :approval-rejected} (:t %)) ledger))

(defn hard-hold-facts
  "HOLD facts carrying at least one governor rule (i.e. not a bare
  phase-gate hold, whose `:basis` is empty)."
  [ledger]
  (filterv #(seq (:basis %)) (hold-facts ledger)))

(defn hold-reasons
  "Every distinct hold REASON the run produced, as sorted strings.
  A governor hold contributes its rule names; a phase hold contributes
  its `:phase-reason`. Derived from the ledger, never enumerated."
  [ledger]
  (->> (hold-facts ledger)
       (mapcat (fn [f] (if (seq (:basis f))
                         (map name (:basis f))
                         (some-> (:phase-reason f) name vector))))
       (remove nil?)
       distinct
       sort
       vec))

(defn approval-grants
  "`:approval-granted` audit facts -- who approved what."
  [ledger]
  (filterv #(= :approval-granted (:t %)) ledger))

(defn- committed-entities
  "Every entity the run actually committed to the SSoT, as
  {:kind .. :id .. :record ..}. Read back out of the store, not out of
  the graph state -- this is what a later reader would actually see."
  [db]
  (concat
   (map (fn [b] {:kind "batch" :id (:id b) :record b}) (store/all-batches db))
   (map (fn [m] {:kind "maintenance" :id (:id m) :record m}) (store/all-maintenance db))
   (map (fn [c] {:kind "safety-concern" :id (:id c) :record c}) (store/safety-concerns db))
   (map (fn [r] (let [id (get r "shipment_id")]
                  {:kind "shipment" :id id :record (store/shipment db id)}))
        (store/shipment-history db))))

(defn approver-survives-store?
  "MEASURED, not assumed: does the approver's identity actually survive
  into the SSoT?

  `printpressmfg.operation`'s `:request-approval` node writes the
  approver into the commit record's `:payload`
  (`{:approved-by ...}`), but `printpressmfg.store`'s `commit-record!`
  destructures `[effect path value]` and never reads `:payload`. This
  function does not encode that conclusion -- it walks the committed
  registers and reports whether the key is present, so the page
  self-corrects if the store is later fixed."
  [db]
  (boolean (some #(contains? (:record %) :approved-by) (committed-entities db))))

;; ---------------------------------------------------------------------------
;; rendering primitives
;; ---------------------------------------------------------------------------

(defn- esc [v]
  (-> (str v)
      (str/replace "&" "&amp;")
      (str/replace "<" "&lt;")
      (str/replace ">" "&gt;")
      (str/replace "\"" "&quot;")))

(defn- fmt2
  "Two-decimal formatting pinned to `Locale/ROOT` -- `clojure.core/
  format` would use the default locale, which decides whether the
  decimal separator is `.` or `,` and would make this build
  machine-dependent."
  [x]
  (String/format java.util.Locale/ROOT "%.2f" (into-array Object [(double x)])))

(defn- tag [class body] (str "<span class=\"" class "\">" body "</span>"))
(defn- ok [s] (tag "ok" (esc s)))
(defn- warn [s] (tag "warn" (esc s)))
(defn- bad [s] (tag "bad" (esc s)))
(defn- muted [s] (tag "muted" (esc s)))
(defn- code [s] (str "<code>" (esc s) "</code>"))

(defn- row [cells]
  (str "        <tr>" (apply str (map #(str "<td>" % "</td>") cells)) "</tr>"))

(defn- rows [coll f]
  (if (seq coll)
    (str/join "\n" (map (comp row f) coll))
    (str "        <tr><td colspan=\"9\">" (muted "no rows") "</td></tr>")))

(defn- table [headers body-rows]
  (str "    <table>\n"
       "      <thead><tr>"
       (apply str (map #(str "<th>" % "</th>") headers))
       "</tr></thead>\n"
       "      <tbody>\n" body-rows "\n      </tbody>\n"
       "    </table>\n"))

(defn- section [title lead body]
  (str "  <section class=\"card\">\n"
       "    <h2>" title "</h2>\n"
       "    <p class=\"lead\">" lead "</p>\n"
       body
       "  </section>\n"))

(defn- yes-no [v] (if (true? v) (ok "yes") (bad "no")))

(defn- kw-str [v] (if (nil? v) "" (str v)))

;; ---------------------------------------------------------------------------
;; CSS -- DADS primitives extracted from this repo's own docs/index.html
;; ---------------------------------------------------------------------------

(def ^:private dds-css
  "Only the デジタル庁デザインシステム (DADS) primitives this console
  actually references, copied verbatim out of the DADS stylesheet this
  repo already vendors into `docs/index.html`, so the console wears the
  same face as the product page and the build stays fully offline (no
  git dependency, no network).

  Deliberately NOT wired through `jp-go-dds`'s `tokens/bridge-css`: that
  bridge maps the `--hig-*` contract, which is not the token vocabulary
  used here, and adding the git dep would make this build
  network-dependent. Semantic aliases below are defined in terms of the
  `-50`/`-100` tint steps -- `--color-semantic-error-1` and `-2` are
  BOTH dark (red-800/red-900) and are not a strong/weak pair."
  (str/join
   "\n"
   [":root{"
    "  --color-neutral-white:#ffffff;"
    "  --color-neutral-black:#000000;"
    "  --color-neutral-solid-gray-50:#f2f2f2;"
    "  --color-neutral-solid-gray-100:#e6e6e6;"
    "  --color-neutral-solid-gray-200:#cccccc;"
    "  --color-neutral-solid-gray-300:#b3b3b3;"
    "  --color-neutral-solid-gray-536:#767676;"
    "  --color-neutral-solid-gray-600:#666666;"
    "  --color-neutral-solid-gray-700:#4d4d4d;"
    "  --color-neutral-solid-gray-800:#333333;"
    "  --color-neutral-solid-gray-900:#1a1a1a;"
    "  --color-primitive-blue-50:#e8f1fe;"
    "  --color-primitive-blue-100:#d9e6ff;"
    "  --color-primitive-blue-200:#c5d7fb;"
    "  --color-primitive-blue-800:#0031d8;"
    "  --color-primitive-blue-900:#0017c1;"
    "  --color-primitive-blue-1000:#00118f;"
    "  --color-primitive-blue-1100:#000071;"
    "  --color-primitive-red-50:#fdeeee;"
    "  --color-primitive-red-100:#ffdada;"
    "  --color-primitive-red-900:#ce0000;"
    "  --color-primitive-red-1000:#a90000;"
    "  --color-primitive-red-1100:#850000;"
    "  --color-primitive-green-50:#e6f5ec;"
    "  --color-primitive-green-100:#c2e5d1;"
    "  --color-primitive-green-900:#115a36;"
    "  --color-primitive-green-1000:#0c472a;"
    "  --color-primitive-orange-50:#ffeee2;"
    "  --color-primitive-orange-100:#ffdfca;"
    "  --color-primitive-orange-1000:#8b3200;"
    "  --color-primitive-orange-1100:#6d2700;"
    ;; semantic aliases, DADS naming, built from the primitives above
    "  --color-key:var(--color-primitive-blue-900);"
    "  --color-key-strong:var(--color-primitive-blue-1100);"
    "  --color-key-tint:var(--color-primitive-blue-50);"
    "  --color-key-border:var(--color-primitive-blue-200);"
    "  --color-success:var(--color-primitive-green-1000);"
    "  --color-success-tint:var(--color-primitive-green-50);"
    "  --color-success-border:var(--color-primitive-green-100);"
    "  --color-error:var(--color-primitive-red-1100);"
    "  --color-error-tint:var(--color-primitive-red-50);"
    "  --color-error-border:var(--color-primitive-red-100);"
    "  --color-warning:var(--color-primitive-orange-1100);"
    "  --color-warning-tint:var(--color-primitive-orange-50);"
    "  --color-warning-border:var(--color-primitive-orange-100);"
    "  --color-text:var(--color-neutral-solid-gray-900);"
    "  --color-text-muted:var(--color-neutral-solid-gray-600);"
    "  --color-border:var(--color-neutral-solid-gray-200);"
    "  --color-border-subtle:var(--color-neutral-solid-gray-100);"
    "  --color-surface:var(--color-neutral-white);"
    "  --color-canvas:var(--color-neutral-solid-gray-50);"
    "  --font-family-sans:'Noto Sans JP','Hiragino Kaku Gothic ProN','Yu Gothic Medium',"
    "    system-ui,-apple-system,'Segoe UI',sans-serif;"
    "  --font-family-mono:'SFMono-Regular',Menlo,Consolas,'Noto Sans Mono',monospace;"
    "  --space-1:4px; --space-2:8px; --space-3:12px; --space-4:16px;"
    "  --space-5:24px; --space-6:32px; --space-8:48px;"
    "  --radius-sm:4px; --radius-md:8px;"
    "}"]))

(def ^:private page-css
  (str/join
   "\n"
   ["*,*::before,*::after{box-sizing:border-box;}"
    "body{margin:0;background:var(--color-canvas);color:var(--color-text);"
    "font-family:var(--font-family-sans);line-height:1.7;"
    "-webkit-font-smoothing:antialiased;}"
    "header.bar{background:var(--color-key-strong);color:var(--color-neutral-white);"
    "padding:var(--space-6) var(--space-5);}"
    "header.bar .wrap{max-width:1180px;margin:0 auto;}"
    "header.bar h1{margin:0 0 var(--space-3);font-size:1.5rem;line-height:1.4;}"
    "header.bar p{margin:0;color:var(--color-primitive-blue-100);font-size:0.9rem;}"
    "header.bar .badge{display:inline-block;margin-top:var(--space-4);"
    "padding:var(--space-1) var(--space-3);border-radius:var(--radius-sm);"
    "background:var(--color-primitive-blue-1000);color:var(--color-neutral-white);"
    "font-size:0.8rem;font-family:var(--font-family-mono);}"
    "main{max-width:1180px;margin:0 auto;padding:var(--space-6) var(--space-5) var(--space-8);}"
    "section.card{background:var(--color-surface);border:1px solid var(--color-border);"
    "border-radius:var(--radius-md);padding:var(--space-5);margin-bottom:var(--space-5);}"
    "section.card h2{margin:0 0 var(--space-2);font-size:1.15rem;color:var(--color-key-strong);}"
    "section.card p.lead{margin:0 0 var(--space-4);color:var(--color-text-muted);"
    "font-size:0.875rem;}"
    "table{width:100%;border-collapse:collapse;font-size:0.85rem;}"
    "thead th{text-align:left;padding:var(--space-2) var(--space-3);"
    "background:var(--color-key-tint);color:var(--color-key-strong);"
    "border-bottom:2px solid var(--color-key-border);white-space:nowrap;font-weight:600;}"
    "tbody td{padding:var(--space-2) var(--space-3);"
    "border-bottom:1px solid var(--color-border-subtle);vertical-align:top;}"
    "tbody tr:last-child td{border-bottom:none;}"
    "code{font-family:var(--font-family-mono);font-size:0.82em;"
    "background:var(--color-neutral-solid-gray-50);color:var(--color-neutral-solid-gray-800);"
    "padding:1px var(--space-1);border-radius:var(--radius-sm);"
    "border:1px solid var(--color-border-subtle);}"
    "span.ok,span.warn,span.bad,span.muted{display:inline-block;"
    "padding:1px var(--space-2);border-radius:var(--radius-sm);font-size:0.8rem;"
    "white-space:nowrap;border:1px solid transparent;}"
    "span.ok{background:var(--color-success-tint);color:var(--color-success);"
    "border-color:var(--color-success-border);}"
    "span.warn{background:var(--color-warning-tint);color:var(--color-warning);"
    "border-color:var(--color-warning-border);}"
    "span.bad{background:var(--color-error-tint);color:var(--color-error);"
    "border-color:var(--color-error-border);}"
    "span.muted{background:var(--color-neutral-solid-gray-50);"
    "color:var(--color-neutral-solid-gray-536);border-color:var(--color-border-subtle);}"
    "dl.kv{display:grid;grid-template-columns:max-content 1fr;gap:var(--space-2) var(--space-4);"
    "margin:0;font-size:0.85rem;}"
    "dl.kv dt{color:var(--color-text-muted);}"
    "dl.kv dd{margin:0;font-family:var(--font-family-mono);"
    "color:var(--color-neutral-solid-gray-800);}"
    "p.note{margin:var(--space-4) 0 0;padding:var(--space-3) var(--space-4);"
    "background:var(--color-warning-tint);border-left:4px solid var(--color-warning);"
    "border-radius:var(--radius-sm);font-size:0.83rem;color:var(--color-neutral-solid-gray-800);}"
    "footer{max-width:1180px;margin:0 auto;padding:0 var(--space-5) var(--space-8);"
    "color:var(--color-text-muted);font-size:0.8rem;}"
    "footer code{background:var(--color-neutral-solid-gray-100);}"]))

;; ---------------------------------------------------------------------------
;; sections
;; ---------------------------------------------------------------------------

(defn- last-fact-for [ledger id]
  (last (filter #(= (:subject %) id) ledger)))

(defn- status-cell [ledger id]
  (let [f (last-fact-for ledger id)]
    (cond
      (nil? f) (muted "no activity")
      (= :committed (:t f)) (ok "committed")
      (= :approval-rejected (:t f)) (warn "rejected by approver")
      (= :governor-hold (:t f))
      (if-let [r (first (:basis f))]
        (bad (str "HARD hold · " (name r)))
        (warn (str "phase hold · " (kw-str (:phase-reason f)))))
      :else (muted "in progress"))))

(defn- batches-section [db ledger]
  (section
   "生産バッチ (production batches) — SSoT"
   (str "Read back out of <code>printpressmfg.store</code> AFTER the run. "
        "<code>shipped</code> is the batch's own cumulative ground truth, which "
        "<code>governor</code> rule <code>:shipment-quantity-exceeded</code> "
        "independently recomputes against — it is never taken from a proposal.")
   (table ["batch" "product-type" "model" "registration acc. (mm)"
           "quantity" "shipped" "defect %" "verified?" "registered?" "last op"]
          (rows (store/all-batches db)
                (fn [b]
                  [(code (:id b)) (code (kw-str (:product-type b))) (esc (:model b))
                   (esc (:registration-accuracy-mm b))
                   (esc (:quantity-units b)) (esc (:shipped-units b))
                   (esc (:defect-rate-percent b))
                   (yes-no (:verified? b)) (yes-no (:registered? b))
                   (status-cell ledger (:id b))])))))

(defn- equipment-section [db ledger]
  (section
   "設備 (fabrication line / commissioning bench) — SSoT"
   (str "<code>bench-002</code> is seeded UNVERIFIED and unregistered, which is why "
        "every maintenance window proposed against it is HARD-held by "
        "<code>:equipment-not-verified</code> — the governor re-derives both flags from "
        "the equipment's own record and never trusts the advisor's rationale.")
   (table ["equipment" "kind" "verified?" "registered?" "last maintenance"
           "last scheduled" "last op"]
          (rows (store/all-equipment db)
                (fn [e]
                  [(code (:id e)) (code (kw-str (:kind e)))
                   (yes-no (:verified? e)) (yes-no (:registered? e))
                   (or (some-> (:last-maintenance-date e) esc) (muted "—"))
                   (or (some-> (:last-scheduled-maintenance-date e) esc) (muted "—"))
                   (status-cell ledger (:id e))])))))

(defn- maintenance-section [db]
  (section
   "保守作業予定ドラフト (maintenance schedule drafts)"
   (str "Committed drafts only. Each carries the immutable "
        "<code>MNT-nnnnnn</code> number minted by "
        "<code>printpressmfg.registry/register-maintenance</code>, and a "
        "dedicated <code>:scheduled?</code> fact — never a "
        "<code>:status</code> value — which backs the "
        "<code>:already-scheduled</code> double-schedule guard.")
   (table ["maintenance" "equipment" "type" "scheduled date" "record no."
           "scheduled?" "approver in record?"]
          (rows (store/all-maintenance db)
                (fn [m]
                  [(code (:id m)) (code (:equipment-id m))
                   (code (kw-str (:maintenance-type m)))
                   (esc (:scheduled-date m))
                   (code (:maintenance-number m))
                   (yes-no (:scheduled? m))
                   (if (contains? m :approved-by)
                     (ok (str (:approved-by m)))
                     (bad "absent"))])))))

(defn- shipments-section [db]
  (section
   "出荷調整ドラフト (shipment coordination drafts)"
   (str "Committed drafts only, in the append-only order "
        "<code>printpressmfg.registry/register-shipment</code> minted them. "
        "<code>ship-2</code> fills <code>batch-001</code> EXACTLY to its recorded "
        "25-unit capacity — legal, and the reason "
        "<code>shipment-quantity-exceeded?</code> compares at 1/10000 of a unit "
        "rather than on raw doubles.")
   (table ["record no." "shipment" "batch" "units" "destination" "approver in record?"]
          (rows (store/shipment-history db)
                (fn [r]
                  (let [id (get r "shipment_id")
                        s (store/shipment db id)]
                    [(code (get r "record_id")) (code id)
                     (code (:batch-id s)) (esc (:units s))
                     (esc (:destination s))
                     (if (contains? s :approved-by)
                       (ok (str (:approved-by s)))
                       (bad "absent"))]))))))

(defn- safety-section [db]
  (section
   "安全懸念ログ (safety-concern log)"
   (str "<code>:flag-safety-concern</code> carries "
        "<code>:stake :coordination/safety-concern</code>, which is permanently in "
        "<code>governor/high-stakes</code>. It ALWAYS escalates to a human "
        "regardless of confidence, and no phase ever lists it in an "
        "<code>:auto</code> set — two independent layers agree.")
   (table ["concern" "equipment" "severity" "description" "approver in record?"]
          (rows (store/safety-concerns db)
                (fn [c]
                  [(code (:id c)) (code (:equipment-id c))
                   (code (kw-str (:severity c))) (esc (:description c))
                   (if (contains? c :approved-by)
                     (ok (str (:approved-by c)))
                     (bad "absent"))])))))

(defn- holds-section [ledger]
  (let [hs (hold-facts ledger)]
    (section
     (str "HARD holds — " (count hs) " held proposals, "
          (count (hold-reasons ledger)) " distinct reasons")
     (str "Every rejection this run produced. A governor HARD hold NEVER reaches a "
          "human and can never be overridden by any phase or approval; a "
          "<code>phase</code> hold is the rollout gate refusing an op the current "
          "phase has not enabled; <code>approver-rejected</code> is a human saying no "
          "to a proposal the governor had already cleared. "
          "Reasons and details are read out of the ledger — not enumerated here.")
     (table ["source" "op" "subject" "rule" "confidence" "detail"]
            (rows hs
                  (fn [f]
                    (let [v (first (:violations f))
                          r (or (:rule v) (:phase-reason f))]
                      [(if (= :approval-rejected (:t f))
                         (warn "human approver")
                         (if (seq (:basis f)) (bad "governor") (warn "phase gate")))
                       (code (kw-str (:op f))) (code (:subject f))
                       (code (kw-str r))
                       (esc (:confidence f))
                       (or (some-> (:detail v) esc)
                           (muted (str "phase " (:phase f) " — "
                                       (kw-str (:phase-reason f)))))])))))))

(defn- rule-coverage-section [ledger]
  (let [reasons (hold-reasons ledger)
        counts (frequencies
                (mapcat (fn [f] (if (seq (:basis f))
                                  (map name (:basis f))
                                  (some-> (:phase-reason f) name vector)))
                        (hold-facts ledger)))]
    (section
     (str "Rule coverage — " (count reasons) " distinct hold reasons exercised")
     (str "Derived by folding the run's own ledger, so this table cannot claim a rule "
          "the scenario did not actually trip. "
          "<code>printpressmfg.governor</code> defines twelve HARD rules; "
          "<code>printpressmfg.phase</code> contributes <code>phase-disabled</code> and "
          "the approval path contributes <code>approver-rejected</code>.")
     (table ["hold reason" "times tripped"]
            (rows reasons (fn [r] [(code r) (esc (get counts r 0))]))))))

(defn- gate-section []
  (section
   "Action gate — rollout phases (<code>printpressmfg.phase/phases</code>)"
   (str "Read directly from the real <code>phases</code> var, not transcribed. "
        "<code>:schedule-maintenance</code> is a member of <code>write-ops</code> but is "
        "deliberately ABSENT from EVERY phase's <code>:auto</code> set including phase 3 — "
        "a permanent structural fact, not a rollout milestone still to come.")
   (table ["phase" "label" "may write" "may auto-commit when governor-clean"]
          (rows (sort (keys phase/phases))
                (fn [p]
                  (let [{:keys [label writes auto]} (get phase/phases p)]
                    [(code p)
                     (str (esc label)
                          (when (= p phase/default-phase)
                            (str " " (ok "default"))))
                     (if (seq writes)
                       (str/join " " (map code (sort (map str writes))))
                       (muted "none"))
                     (if (seq auto)
                       (str/join " " (map code (sort (map str auto))))
                       (muted "none — every write needs a human"))]))))))

(defn- contract-section []
  (section
   "Governor contract (<code>printpressmfg.governor</code>)"
   (str "The closed allowlists and the confidence floor, read out of the real vars. "
        "A proposal whose own <code>:effect</code> falls outside the effect allowlist is "
        "direct fabrication/assembly-line-equipment control — this actor's central, "
        "permanent scope boundary.")
   (str "    <dl class=\"kv\">\n"
        "      <dt>allowed ops</dt><dd>"
        (str/join " " (map code (sort (map str governor/allowed-ops)))) "</dd>\n"
        "      <dt>allowed proposal effects</dt><dd>"
        (str/join " " (map code (sort (map str governor/allowed-proposal-effects)))) "</dd>\n"
        "      <dt>always-human stakes</dt><dd>"
        (str/join " " (map code (sort (map str governor/high-stakes)))) "</dd>\n"
        "      <dt>confidence floor</dt><dd>" (esc governor/confidence-floor) "</dd>\n"
        "    </dl>\n")))

(defn- attribution-section [db ledger]
  (let [grants (approval-grants ledger)
        survives? (approver-survives-store? db)]
    (section
     "Approver attribution — audit ledger vs. SSoT"
     (str "MEASURED at render time by walking every committed register and asking "
          "whether the <code>:approved-by</code> key is actually present, so this "
          "section self-corrects if the store is later fixed. It is not hardcoded.")
     (str
      (table ["op" "subject" "approved by (audit ledger)" "approver in commit record?"]
             (rows grants
                   (fn [f]
                     [(code (kw-str (:op f))) (code (:subject f))
                      (ok (str (:by f)))
                      (if survives?
                        (ok "present")
                        (bad "absent (audit only — not in commit record)"))])))
      (if survives?
        (str "    <p class=\"note\">Measured: the approver's identity DOES survive into "
             "the SSoT. Both the append-only ledger and the committed register name "
             "the human who approved each write.</p>\n")
        (str "    <p class=\"note\">Measured on this run: the approver's identity does "
             "NOT survive into the SSoT. <code>printpressmfg.operation</code>'s "
             "<code>:request-approval</code> node writes it into the commit record's "
             "<code>:payload</code> as <code>:approved-by</code>, but "
             "<code>printpressmfg.store/commit-record!</code> destructures "
             "<code>[effect path value]</code> and never reads <code>:payload</code>, so "
             "the key is dropped on the way in. Attribution above is therefore joined "
             "from the <code>:approval-granted</code> audit facts and labelled "
             "<em>audit only</em> — &ldquo;nobody approved&rdquo; and &ldquo;the store "
             "did not keep it&rdquo; must not look the same to a reader.</p>\n"))))))

(defn- ledger-section [ledger]
  (section
   (str "Audit ledger — " (count ledger) " immutable decision facts")
   (str "Append-only, in the order the run wrote it. Every proposal that reached a "
        "disposition appears here exactly once: commits, holds, and the approval "
        "grant/rejection that preceded them.")
   (table ["#" "fact" "op" "subject" "disposition" "basis / approver"]
          (rows (map-indexed vector ledger)
                (fn [[i f]]
                  [(esc (inc i)) (code (kw-str (:t f))) (code (kw-str (:op f)))
                   (code (:subject f))
                   (case (:t f)
                     :committed (ok "commit")
                     :approval-granted (ok "approved")
                     :approval-rejected (warn "rejected")
                     :governor-hold (if (seq (:basis f)) (bad "HARD hold") (warn "phase hold"))
                     (muted (kw-str (:disposition f))))
                   (cond
                     (:by f) (code (:by f))
                     (seq (:basis f)) (str/join " " (map (comp code name) (:basis f)))
                     (:phase-reason f) (code (kw-str (:phase-reason f)))
                     :else (muted "—"))])))))

(defn- catalog-section []
  (section
   "製造ユニット型式 (<code>printpressmfg.facts/unit-types</code>)"
   (str "The manufactured unit models this ISIC 2829 plant produces. "
        "Resolution and depth-of-focus below are COMPUTED live by the repo's own "
        "<code>facts/resolution-nm</code> (Rayleigh CD = k1·λ/NA) and "
        "<code>facts/depth-of-focus-nm</code> (DOF = k2·λ/NA²) from the catalog's "
        "own wavelength and NA — they are not quoted spec numbers. The GTIN column "
        "is checked by the repo's own GS1 Modulo-10 implementation.")
   (table ["unit type" "name" "λ (nm)" "NA" "NA ceiling (medium)"
           "CD @ k1=0.25 (nm)" "CD @ k1=0.30 (nm)" "DOF @ k2=1.0 (nm)"
           "UNSPSC" "GTIN" "GTIN check"]
          (rows (facts/unit-type-ids)
                (fn [id]
                  (let [u (facts/unit-type-by-id id)]
                    [(code id) (esc (:name u))
                     (esc (:exposure-wavelength-nm u)) (esc (:numerical-aperture u))
                     (str (esc (:immersion-medium-index u)) " "
                          (muted (:immersion-medium u)))
                     (esc (fmt2 (facts/resolution-nm u 0.25)))
                     (esc (fmt2 (facts/resolution-nm u 0.30)))
                     (esc (fmt2 (facts/depth-of-focus-nm u 1.0)))
                     (code (:unspsc-code u))
                     (code (:gtin u))
                     (if (facts/gtin13-valid? (:gtin u))
                       (ok "mod-10 valid")
                       (bad "mod-10 invalid"))]))))))

;; ---------------------------------------------------------------------------
;; document
;; ---------------------------------------------------------------------------

(defn render
  "Renders the full operator console from a store `db` that has already
  been driven by `run-demo!` (or any other real scenario)."
  [db]
  (let [ledger (vec (store/ledger db))
        holds (hold-facts ledger)
        reasons (hold-reasons ledger)]
    (str
     "<!DOCTYPE html>\n"
     "<html lang=\"ja\">\n<head>\n"
     "<meta charset=\"utf-8\">\n"
     "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
     "<title>cloud-itonami-isic-2829 · 特殊用途機械プラント運用 Operator Console</title>\n"
     "<style>\n" dds-css "\n" page-css "\n</style>\n"
     "</head>\n<body>\n"
     "<header class=\"bar\">\n"
     "  <div class=\"wrap\">\n"
     "    <h1>その他の特殊目的用機械の製造 (ISIC 2829) — Operator Console</h1>\n"
     "    <p>Special-Purpose Machinery Plant Operations Governor · industrial printing-press &amp; semiconductor-lithography manufacturing · propose-only actor</p>\n"
     "    <span class=\"badge\">"
     (esc (str "generated · " (count ledger) " ledger facts · " (count holds)
               " holds · " (count reasons) " distinct hold reasons"))
     "</span>\n"
     "  </div>\n"
     "</header>\n"
     "<main>\n"
     (batches-section db ledger)
     (equipment-section db ledger)
     (maintenance-section db)
     (shipments-section db)
     (safety-section db)
     (holds-section ledger)
     (rule-coverage-section ledger)
     (gate-section)
     (contract-section)
     (attribution-section db ledger)
     (ledger-section ledger)
     (catalog-section)
     "</main>\n"
     "<footer>\n"
     "  <p>Build-time artifact — generated by <code>printpressmfg.render-html</code> "
     "(<code>clojure -M:dev:render-html</code>) by running the real "
     "<code>printpressmfg.operation</code> StateGraph over a freshly seeded "
     "<code>printpressmfg.store/sample-data!</code>. No hand-written rows, no "
     "timestamps, no randomness: two consecutive runs are byte-identical. "
     "This actor only ever PROPOSES — it never actuates fabrication or "
     "assembly-line equipment, never dispatches a freight carrier, and never "
     "issues a CE (2006/42/EC) or ANSI B65.1 machinery-safety conformity mark.</p>\n"
     "</footer>\n"
     "</body>\n</html>\n")))

(defn -main [& args]
  (let [out (or (first args) "docs/samples/operator-console.html")
        db (run-demo!)
        ledger (vec (store/ledger db))
        holds (hold-facts ledger)
        hard (hard-hold-facts ledger)
        reasons (hold-reasons ledger)]

    ;; Build-time invariant, not a convention: a console that renders no
    ;; hold would depict an ungoverned actor.
    (when (empty? holds)
      (throw (ex-info "render-html: run produced ZERO :governor-hold records -- refusing to write a console that would depict an ungoverned actor"
                      {:ledger-facts (count ledger) :holds 0})))
    (when (empty? hard)
      (throw (ex-info "render-html: no HARD governor hold in the run (only phase/approval holds) -- the governor was never exercised"
                      {:holds (count holds)})))
    (when (< (count reasons) min-distinct-hold-rules)
      (throw (ex-info "render-html: too few distinct hold reasons -- the scenario stopped exercising the compliance layer"
                      {:required min-distinct-hold-rules
                       :observed (count reasons)
                       :reasons reasons})))

    (let [html (render db)]
      (.mkdirs (java.io.File. (or (.getParent (java.io.File. ^String out)) ".")))
      (spit out html)
      (println "wrote" out)
      (println "  ledger facts        :" (count ledger))
      (println "  holds               :" (count holds) "(" (count hard) "HARD )")
      (println "  distinct hold rules :" (count reasons) (pr-str reasons))
      (println "  batches             :" (count (store/all-batches db)))
      (println "  equipment           :" (count (store/all-equipment db)))
      (println "  maintenance drafts  :" (count (store/all-maintenance db)))
      (println "  shipment drafts     :" (count (store/shipment-history db)))
      (println "  safety concerns     :" (count (store/safety-concerns db)))
      (println "  approver in store   :" (approver-survives-store? db)))))
