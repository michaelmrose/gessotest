(ns gessotest.simple-shared-counter
  (:require
   [gesso.live.core :as live]
   [gesso.live.consistency.xtdb :as live.xtdb :refer [->synced live-read live-swap!]]))

;; 1. The Pure Data Definition

(def counter
  (->synced
   {:table :demo_counters
    :id "global-shared-counter"
    :col :demo/value
    ;; Tell the event bus what we are actually updating
    :entity-type :demo-counter
    :default 0}))

(def live-config
  {:subscription/token "shared-counter"
   :fragment/id "simple-shared-counter-fragment"
   :fragment/src "/app/demo/simple-shared-counter/fragment"
   :fragment/swap "innerHTML"})

(defn button-class []
  "inline-flex h-11 w-11 items-center justify-center rounded-xl border border-border bg-background text-xl font-semibold hover:bg-muted")

(defn counter-button [ctx {:keys [to label]}]
  (live/post-button ctx
   {:to to
    :target (:fragment/id live-config)
    :swap (:fragment/swap live-config)
    :label label
    :button-attrs {:class (button-class)}}))

;; 2. The UI Fragment
(defn fragment [ctx]
  (let [n (live-read ctx counter)]
    [:section {:class "mx-auto max-w-3xl py-6"}
     [:div {:class "rounded-2xl border border-border bg-card text-card-foreground shadow-sm p-6 space-y-5"}
      [:div {:class "space-y-2 text-center"}
       [:div {:class "font-body text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground"}
        "Live Demo (Simple)"]
       [:h2 {:class "font-heading leading-heading tracking-heading text-2xl font-bold"}
        "Shared Counter"]
       [:p {:class "font-body leading-body text-muted-foreground text-base-theme"}
        "Powered by ->synced and live-swap!."]]

      [:div {:class "flex items-center justify-center gap-4"}
       (counter-button ctx {:to "/app/demo/simple-shared-counter/decrement" :label "−"})

       [:div {:class "min-w-28 rounded-xl bg-muted px-6 py-4 text-center"}
        [:div {:class "font-body text-xs uppercase tracking-[0.16em] text-muted-foreground"} "Value"]
        [:div {:class "font-heading text-3xl font-bold"} n]]

       (counter-button ctx {:to "/app/demo/simple-shared-counter/increment" :label "+"})]

      [:p {:class "text-center font-body text-sm text-muted-foreground"}
       "Updates are persisted and pushed live to all viewers."]]]))

;; 3. The Injection Point
(defn section []
  (live/fragment-panel live-config))

;; 4. The Mutations
(defn increment! [ctx]
  (live-swap! ctx counter inc)
  (fragment ctx))

(defn decrement! [ctx]
  (live-swap! ctx counter dec)
  (fragment ctx))
