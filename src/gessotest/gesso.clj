(ns gessotest.gesso
  (:require [clojure.string :as str]
            [gessotest.util :refer :all]
            [gessotest.accordion :as accordion]
            ))

;; public api
(def accordion accordion/accordion)
(def accordion-item accordion/accordion-item)
(def accordion-trigger accordion/accordion-trigger)
(def accordion-content accordion/accordion-content)

;; ---------------------------------------------------------------------------
;; Card Subcomponents
;; ---------------------------------------------------------------------------

(defn card-title [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))]
      (el :h2 {:class class} attrs (nodes (:text props))))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :h2 {:class class} attrs children))))

(defn card-description [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))]
      (el :p {:class class} attrs (nodes (:text props))))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :p {:class class} attrs children))))

(defn card-header [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))
          {:keys [title description children]} props]
      (el :header {:class class} attrs
          [(when title (card-title {} title))
           (when description (card-description {} description))
           (nodes children)]))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :header {:class class} attrs children))))

(defn card-content [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))]
      (el :section {:class class} attrs (nodes (:children props))))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :section {:class class} attrs children))))

(defn card-footer [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))]
      (el :footer {:class class} attrs (nodes (:children props))))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :footer {:class class} attrs children))))


;; ---------------------------------------------------------------------------
;; Card Component
;; ---------------------------------------------------------------------------

(defn card
  "Long form:
    (card {:class ... :attrs ...} children...)

  Short form (map-only):
    (card {:class ... :attrs ... :header <node> :title <node> :description <node> :content <node|seq> :footer <node|seq>})"
  [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))
          {:keys [header title description content footer]} props
          header-node (or header
                          (when (or title description)
                            (card-header {}
                              (when title (card-title {} title))
                              (when description (card-description {} description)))))
          content-node (when (some? content)
                         (apply card-content {} (nodes content)))
          footer-node (when (some? footer)
                        (apply card-footer {} (nodes footer)))]
      (el :div {:class (class-names "card" class)} attrs
          [header-node content-node footer-node]))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :div {:class (class-names "card" class)} attrs children))))


;; ---------------------------------------------------------------------------
;; Accordion Components
;; ---------------------------------------------------------------------------

;; ---------------------------------------------------------------------------
;; Button Component
;; ---------------------------------------------------------------------------

(def ^:private button-classes
  {[:default :md] "btn"
   [:default :sm] "btn-sm"
   [:default :lg] "btn-lg"
   [:default :icon] "btn-icon"
   [:default :sm-icon] "btn-sm-icon"
   [:default :lg-icon] "btn-lg-icon"

   [:primary :md] "btn-primary"
   [:primary :sm] "btn-sm-primary"
   [:primary :lg] "btn-lg-primary"
   [:primary :icon] "btn-icon-primary"
   [:primary :sm-icon] "btn-sm-icon-primary"
   [:primary :lg-icon] "btn-lg-icon-primary"

   [:secondary :md] "btn-secondary"
   [:secondary :sm] "btn-sm-secondary"
   [:secondary :lg] "btn-lg-secondary"
   [:secondary :icon] "btn-icon-secondary"
   [:secondary :sm-icon] "btn-sm-icon-secondary"
   [:secondary :lg-icon] "btn-lg-icon-secondary"

   [:outline :md] "btn-outline"
   [:outline :sm] "btn-sm-outline"
   [:outline :lg] "btn-lg-outline"
   [:outline :icon] "btn-icon-outline"
   [:outline :sm-icon] "btn-sm-icon-outline"
   [:outline :lg-icon] "btn-lg-icon-outline"

   [:ghost :md] "btn-ghost"
   [:ghost :sm] "btn-sm-ghost"
   [:ghost :lg] "btn-lg-ghost"
   [:ghost :icon] "btn-icon-ghost"
   [:ghost :sm-icon] "btn-sm-icon-ghost"
   [:ghost :lg-icon] "btn-lg-icon-ghost"

   [:link :md] "btn-link"
   [:link :sm] "btn-sm-link"
   [:link :lg] "btn-lg-link"
   [:link :icon] "btn-icon-link"
   [:link :sm-icon] "btn-sm-icon-link"
   [:link :lg-icon] "btn-lg-icon-link"

   [:destructive :md] "btn-destructive"
   [:destructive :sm] "btn-sm-destructive"
   [:destructive :lg] "btn-lg-destructive"
   [:destructive :icon] "btn-icon-destructive"
   [:destructive :sm-icon] "btn-sm-icon-destructive"
   [:destructive :lg-icon] "btn-lg-icon-destructive"})

(defn button
  "Long form:
    (button {:class ... :attrs ... :variant ... :size ...} children...)

  Short form (map-only):
    (button {:variant ... :size ... :text <node|scalar>})"
  [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))
          {:keys [variant size text]} props
          variant (or variant :default)
          size (or size :md)
          cls (get button-classes [variant size] "btn")
          attrs' (merge {:type "button"} attrs)]
      (el :button {:class (class-names cls class)} attrs' (nodes text)))
    (let [[maybe-opts & children] args
          opts (if (map? maybe-opts) maybe-opts {})
          {:keys [props class attrs]} (split-opts opts)
          {:keys [variant size]} props
          variant (or variant :default)
          size (or size :md)
          cls (get button-classes [variant size] "btn")
          children (if (map? maybe-opts) children args)
          attrs' (merge {:type "button"} attrs)]
      (el :button {:class (class-names cls class)} attrs' children))))
