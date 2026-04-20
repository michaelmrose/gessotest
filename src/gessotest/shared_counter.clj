(ns gessotest.shared-counter
  (:require [gesso.live.dsl :refer [defsynced defoperation]]
            [gesso.live.core :as live]))

(defsynced counter
  {:path [:demo_counters "global-shared-counter" :demo/value]
   :default 0})

(defoperation increment! [ctx] (swap! counter inc))
(defoperation decrement! [ctx] (swap! counter dec))

(def live-config
  {:subscription/token "shared-counter"
   :fragment/id "shared-counter"
   :fragment/src "/app/demo/shared-counter/fragment"
   :fragment/swap "outerHTML"})

(defn fragment [ctx]
  (let [ctx (merge ctx live-config)
        {:keys [params anti-forgery-token]} ctx] ;; Pull token from ctx

    ;; Handle operations
    (case (get params "op")
      "inc" (increment! ctx)
      "dec" (decrement! ctx)
      nil)

    ;; Render UI
    (let [n (counter-value ctx)]
      [:div.flex.items-center.gap-4
       [:button.btn {:hx-post (str (:fragment/src live-config) "?op=inc")
                     :hx-target "closest div"
                     :hx-swap (:fragment/swap live-config)
                     ;; Use the token we pulled from ctx
                     :hx-vals (str "{\"__anti_forgery_token\": \"" anti-forgery-token "\"}")} "+"]

       [:span.text-2xl.font-mono n]

       [:button.btn {:hx-post (str (:fragment/src live-config) "?op=dec")
                     :hx-target "closest div"
                     :hx-swap (:fragment/swap live-config)
                     :hx-vals (str "{\"__anti_forgery_token\": \"" anti-forgery-token "\"}")} "-"]])))

(defn section [ctx]
  (live/fragment-panel ctx live-config))
