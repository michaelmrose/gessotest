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
  "inline-flex h-11 w-11 items-center justify-center radius-xl border-theme font-heading text-xl-theme weight-semibold-theme")

(defn counter-button [ctx {:keys [to label]}]
  (live/post-button ctx
   {:to to
    :target (:fragment/id live-config)
    :swap (:fragment/swap live-config)
    :label label
    :button-attrs {:class (button-class)
                   :style {:border-style "solid"
                           :border-color "var(--border)"
                           :background "var(--background)"
                           :color "var(--foreground)"}}}))

;; 2. The UI Fragment
(defn fragment [ctx]
  (let [n (live-read ctx counter)]
    [:section {:class "mx-auto max-w-3xl py-6"}
     [:div {:class "radius-xl border-theme pad-card content-stack-theme shadow-sm"
            :style {:border-style "solid"
                    :border-color "var(--border)"
                    :background "var(--card)"
                    :color "var(--card-foreground)"}}

      [:div {:class "title-stack-theme"
             :style {:text-align "center"}}
       [:div {:class "font-body text-sm-theme weight-medium-theme tracking-wide-theme uppercase"
              :style {:color "var(--muted-foreground)"}}
        "Live Demo (Simple)"]
       [:h2 {:class "font-heading leading-heading tracking-heading text-2xl-theme weight-bold-theme"}
        "Shared Counter"]
       [:p {:class "font-body leading-body text-base-theme"
            :style {:color "var(--muted-foreground)"}}
        "Powered by ->synced and live-swap!."]]

      [:div {:class "flex items-center justify-center gap-4"}
       (counter-button ctx {:to "/app/demo/simple-shared-counter/decrement" :label "−"})

       [:div {:class "min-w-28 radius-xl px-6 py-4 text-center"
              :style {:background "var(--muted)"}}
        [:div {:class "font-body text-xs-theme tracking-wide-theme uppercase"
               :style {:color "var(--muted-foreground)"}}
         "Value"]
        [:div {:class "font-heading text-3xl-theme weight-bold-theme"} n]]

       (counter-button ctx {:to "/app/demo/simple-shared-counter/increment" :label "+"})]

      [:p {:class "text-center font-body text-sm-theme leading-body"
           :style {:color "var(--muted-foreground)"}}
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
