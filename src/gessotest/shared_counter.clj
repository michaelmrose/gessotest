(ns gessotest.shared-counter
  (:require
   [gesso.live.core :as live]
   [gesso.live.consistency.xtdb :as live.xtdb]))

(def counter-id "global-shared-counter")

(defn subscription []
  "shared-counter")

(defn stream-url []
  (str "/gesso/live/stream?subscription=" (subscription)))

(defn query []
  ["SELECT _id, demo$value
    FROM demo_counters
    WHERE _id = ?"
   counter-id])

(defn row
  [ctx]
  (or (first (live.xtdb/q ctx (query)))
      {:xt/id counter-id
       :demo$value 0}))

(defn value
  [ctx]
  (let [r (row ctx)]
    (or (:demo$value r)
        (:demo/value r)
        0)))

(defn fragment
  [ctx]
  (let [n (value ctx)]
    [:section {:id "shared-counter-fragment"
               :class "mx-auto max-w-3xl py-6"}
     [:div {:class "rounded-2xl border border-border bg-card text-card-foreground shadow-sm p-6 space-y-5"}
      [:div {:class "space-y-2 text-center"}
       [:div {:class "font-body text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground"}
        "Live Demo"]
       [:h2 {:class "font-heading leading-heading tracking-heading text-2xl font-bold"}
        "Shared Counter"]
       [:p {:class "font-body leading-body text-muted-foreground text-base-theme"}
        "All signed-in users see and change the same persisted value."]]

      [:div {:class "flex items-center justify-center gap-4"}
       [:button {:type "button"
                 :hx-get "/app/demo/shared-counter/decrement"
                 :hx-target "#shared-counter-fragment"
                 :hx-swap "outerHTML"
                 :class "inline-flex h-11 w-11 items-center justify-center rounded-xl border border-border bg-background text-xl font-semibold hover:bg-muted"}
        "−"]

       [:div {:class "min-w-28 rounded-xl bg-muted px-6 py-4 text-center"}
        [:div {:class "font-body text-xs uppercase tracking-[0.16em] text-muted-foreground"}
         "Value"]
        [:div {:class "font-heading text-3xl font-bold"}
         n]]

       [:button {:type "button"
                 :hx-get "/app/demo/shared-counter/increment"
                 :hx-target "#shared-counter-fragment"
                 :hx-swap "outerHTML"
                 :class "inline-flex h-11 w-11 items-center justify-center rounded-xl border border-border bg-background text-xl font-semibold hover:bg-muted"}
        "+"]]

      [:p {:class "text-center font-body text-sm text-muted-foreground"}
       "Updates are persisted and pushed live to all viewers."]]]))

(defn section
  []
  (live/fragment
   {:id "shared-counter-fragment"
    :src "/app/demo/shared-counter/fragment"
    :stream-url (stream-url)}))

(defn fragment-handler
  [ctx]
  (fragment ctx))

#_(defn- write-value!
  [ctx new-value reason]
  (prn :shared-counter/write-start
       :new-value new-value
       :reason reason
       :bus (:gesso.live/bus ctx))
  (try
    (let [result
          (live.xtdb/submit-tx!
           ctx
           {:tx [[:put-docs :demo_counters
                  {:xt/id counter-id
                   :demo/value new-value}]]
            :changed {:entity/type :demo-counter
                      :entity/id counter-id
                      :change/kind :updated}
            :data {:reason reason}})]
      (prn :shared-counter/write-success result)
      result)
    (catch Throwable t
      (prn :shared-counter/write-failed
           {:message (.getMessage t)
            :class (class t)
            :data (ex-data t)})
      (throw t))))

#_(defn- write-value!
  [ctx new-value _reason]
  (live.xtdb/submit-tx-raw!
   (live.xtdb/connectable-from-ctx ctx)
   [[:put-docs :demo_counters
     {:xt/id counter-id
      :demo/value new-value}]]))

(defn- write-value!
  [ctx new-value reason]
  (live.xtdb/submit-tx!
   ctx
   {:tx [[:put-docs :demo_counters
          {:xt/id counter-id
           :demo/value new-value}]]
    :changed {:entity/type :demo-counter
              :entity/id counter-id
              :change/kind :updated}
    :data {:reason reason}}))

(defn increment!
  [ctx]
  (write-value! ctx (inc (value ctx)) :increment)
  (fragment ctx))

(defn decrement!
  [ctx]
  (write-value! ctx (dec (value ctx)) :decrement)
  (fragment ctx))
