(ns gessotest.live-playground
  (:require
   [gesso.live.core :as live]
   [gesso.live.bus :as bus]))

(def mock-bus
  (bus/memory-bus))

(def ctx
  {:gesso.live/bus mock-bus
   :headers {"x-gesso-live-consistency-token" [:tx 9]}})

(def fragment-example
  (live/fragment
   {:id "request-panel"
    :src "/app/fake/request-fragment"
    :stream-url "/gesso/live/stream?subscription=fake"}))

(def event-example
  (live/build-event
   {:changed {:entity/type :request
              :entity/id "req-123"
              :change/kind :updated}}))

(defn publish-example! []
  (live/publish-change!
   ctx
   {:changed {:entity/type :request
              :entity/id "req-123"
              :change/kind :updated}}))
