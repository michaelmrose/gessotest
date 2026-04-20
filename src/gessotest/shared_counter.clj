(ns gessotest.shared-counter
  (:require
   [gesso.live.core :as live]
   [gesso.live.consistency.xtdb :as live.xtdb]))

(def counter-id "global-shared-counter")

(def live-config
  {:subscription/token "shared-counter"
   :entry [:demo-counter counter-id]
   :fragment/id "shared-counter-fragment"
   :fragment/src "/app/demo/shared-counter/fragment"
   :fragment/swap "innerHTML"})

(defn query
  []
  ["SELECT _id, demo$value
    FROM demo_counters
    WHERE _id = ?"
   counter-id])

(defn extract-counter-value
  [row]
  (or (:demo$value row)
      0))

(defn value
  [ctx]
  (extract-counter-value
   (first (live.xtdb/q ctx (query)))))

(defn button-class
  []
  "inline-flex h-11 w-11 items-center justify-center rounded-xl border border-border bg-background text-xl font-semibold hover:bg-muted")

(defn counter-button
  [ctx {:keys [to label]}]
  (live/post-button
   ctx
   {:to to
    :target (:fragment/id live-config)
    :swap (:fragment/swap live-config)
    :label label
    :button-attrs {:class (button-class)}}))

(defn fragment
  [ctx]
  (let [n (value ctx)]
    [:section {:class "mx-auto max-w-3xl py-6"}
     [:div {:class "rounded-2xl border border-border bg-card text-card-foreground shadow-sm p-6 space-y-5"}
      [:div {:class "space-y-2 text-center"}
       [:div {:class "font-body text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground"}
        "Live Demo"]
       [:h2 {:class "font-heading leading-heading tracking-heading text-2xl font-bold"}
        "Shared Counter"]
       [:p {:class "font-body leading-body text-muted-foreground text-base-theme"}
        "All signed-in users see and change the same persisted value."]]

      [:div {:class "flex items-center justify-center gap-4"}
       (counter-button
        ctx
        {:to "/app/demo/shared-counter/decrement"
         :label "−"})

       [:div {:class "min-w-28 rounded-xl bg-muted px-6 py-4 text-center"}
        [:div {:class "font-body text-xs uppercase tracking-[0.16em] text-muted-foreground"}
         "Value"]
        [:div {:class "font-heading text-3xl font-bold"}
         n]]

       (counter-button
        ctx
        {:to "/app/demo/shared-counter/increment"
         :label "+"})]

      [:p {:class "text-center font-body text-sm text-muted-foreground"}
       "Updates are persisted and pushed live to all viewers."]]]))

(defn section
  []
  (live/fragment-panel live-config))

(defn increment!
  [ctx]
  (let [new-value (inc (value ctx))]
    (live.xtdb/put-value-and-publish!
     ctx
     {:table :demo_counters
      :doc {:xt/id counter-id
            :demo/value new-value}
      :query (query)
      :extract-value extract-counter-value
      :expected-value new-value
      :changed {:entity/type :demo-counter
                :entity/id counter-id
                :change/kind :updated}
      :data {:reason :increment}})
    (fragment ctx)))

(defn decrement!
  [ctx]
  (let [new-value (dec (value ctx))]
    (live.xtdb/put-value-and-publish!
     ctx
     {:table :demo_counters
      :doc {:xt/id counter-id
            :demo/value new-value}
      :query (query)
      :extract-value extract-counter-value
      :expected-value new-value
      :changed {:entity/type :demo-counter
                :entity/id counter-id
                :change/kind :updated}
      :data {:reason :decrement}})
    (fragment ctx)))
