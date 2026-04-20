(ns gessotest.shared-counter
  (:require [gesso.live.dsl :refer [defsynced defoperation]]
            [gesso.live.core :as live]
            [ring.util.anti-forgery :refer [anti-forgery-token]]))

;; 1. The Data Definition
(defsynced counter
  {:path [:demo_counters "global-shared-counter" :demo/value]
   :default 0})

;; 2. The Operations
(defoperation increment! [ctx] (swap! counter inc))
(defoperation decrement! [ctx] (swap! counter dec))

;; 3. The Live Configuration
(def live-config
  {:subscription/token "shared-counter"
   :fragment/id "shared-counter"
   :fragment/src "/app/demo/shared-counter/fragment"
   :fragment/swap "outerHTML"})

;; 4. The Fragment (Controller + View)
(defn fragment [{:keys [params] :as ctx}]
  ;; Gateway logic: Intercept the POST action
  (case (get params "op")
    "inc" (increment! ctx)
    "dec" (decrement! ctx)
    nil)

  (let [n (counter-value ctx)
        ;; Biff provides this in the request map
        token (force anti-forgery-token)]
    [:div.flex.items-center.gap-4
     ;; We use hx-vals to send the CSRF token Biff expects
     [:button.btn {:hx-post (str (:fragment/src live-config) "?op=inc")
                   :hx-target "closest div"
                   :hx-swap (:fragment/swap live-config)
                   :hx-vals (str "{\"__anti_forgery_token\": \"" token "\"}")} "+"]

     [:span.text-2xl.font-mono n]

     [:button.btn {:hx-post (str (:fragment/src live-config) "?op=dec")
                   :hx-target "closest div"
                   :hx-swap (:fragment/swap live-config)
                   :hx-vals (str "{\"__anti_forgery_token\": \"" token "\"}")} "-"]]))

;; 5. The Main Page Section
(defn section [ctx]
  (live/fragment-panel ctx live-config))
