;; (ns gessotest.live
;;   (:require
;;    [gesso.live.bus :as bus]
;;    [gesso.live.transport.sse :as sse]))

;; (def shared-counter-id
;;   "global-shared-counter")

;; (defn parse-subscription
;;   "Turn a raw request subscription token into the app-level subscription value.

;;    For now this namespace only knows about the shared counter demo."
;;   [raw-subscription]
;;   (let [parsed
;;         (case raw-subscription
;;           "shared-counter"
;;           {:kind :demo-counter
;;            :id shared-counter-id}

;;           nil)]
;;     (prn :live/parse-subscription
;;          :raw raw-subscription
;;          :parsed parsed)
;;     parsed))

;; (def matcher
;;   {:subscription->entries
;;    (fn [subscription]
;;      (let [entries
;;            (case (:kind subscription)
;;              :demo-counter
;;              [[:demo-counter (:id subscription)]]

;;              [])]
;;        (prn :live/subscription->entries
;;             :subscription subscription
;;             :entries entries)
;;        entries))

;;    :changed->entries
;;    (fn [_ctx changed]
;;      (let [entries
;;            (case (:entity/type changed)
;;              :demo-counter
;;              [[:demo-counter (:entity/id changed)]]

;;              [])]
;;        (prn :live/changed->entries
;;             :changed changed
;;             :entries entries)
;;        entries))})

;; (defonce live-bus
;;   (do
;;     (prn :live/init-bus)
;;     (bus/memory-bus matcher)))

;; (defn wrap-live-bus
;;   "Assoc the shared live bus into request ctx so live publication can find it."
;;   [handler]
;;   (fn [ctx]
;;     (prn :live/wrap-live-bus
;;          :uri (:uri ctx)
;;          :request-method (:request-method ctx))
;;     (handler (assoc ctx :gesso.live/bus live-bus))))

;; (defn subscription-from-ctx
;;   "Read the raw subscription request param and parse it into an app subscription."
;;   [ctx]
;;   (let [raw (or (get-in ctx [:params "subscription"])
;;                 (get-in ctx [:params :subscription])
;;                 (get-in ctx [:query-params "subscription"])
;;                 (get-in ctx [:query-params :subscription]))
;;         parsed (parse-subscription raw)]
;;     (prn :live/subscription-from-ctx
;;          :params (:params ctx)
;;          :query-params (:query-params ctx)
;;          :raw raw
;;          :parsed parsed)
;;     parsed))

;; (defn sse-handler
;;   [ctx]
;;   (let [subscription (subscription-from-ctx ctx)]
;;     (prn :live/sse-handler
;;          :uri (:uri ctx)
;;          :request-method (:request-method ctx)
;;          :params (:params ctx)
;;          :subscription subscription)
;;     (sse/handler
;;      {:ctx ctx
;;       :subscription-fn (constantly subscription)})))
