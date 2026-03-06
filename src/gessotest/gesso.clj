(ns gessotest.gesso
  (:require [clojure.string :as str]))

;; ---------------------------------------------------------------------------
;; Minimal Hiccup helpers
;; ---------------------------------------------------------------------------

(defn- only-map-arg?
  [args]
  (and (= 1 (count args))
       (map? (first args))))

(defn class-names
  "Join class values into a single class string.
  Accepts strings, keywords, nils, and nested sequential values."
  [& xs]
  (->> xs
       flatten
       (remove nil?)
       (mapcat (fn [x]
                 (cond
                   (string? x) [x]
                   (keyword? x) [(name x)]
                   (sequential? x) x
                   :else [(str x)])))
       (remove str/blank?)
       (str/join " ")))

(defn merge-attrs
  "Merge Hiccup attribute maps, concatenating :class instead of overwriting it."
  [& maps]
  (reduce
   (fn [acc m]
     (let [m (or m {})]
       (-> acc
           (merge (dissoc m :class))
           (update :class class-names (:class acc) (:class m)))))
   {}
   maps))

(defn- normalize-children
  "Flatten child sequences one level while preserving hiccup vectors as atomic.
  - nil children are removed
  - lists/sequences from (for ...) are spliced
  - hiccup vectors are kept intact"
  [children]
  (->> children
       (mapcat (fn [c]
                 (cond
                   (nil? c) []
                   (vector? c) [c]
                   (and (sequential? c) (not (string? c))) c
                   :else [c])))
       (remove nil?)))

(defn- hiccup-element?
  "True if x looks like a hiccup element vector: [tag ...] where tag is a keyword or symbol."
  [x]
  (and (vector? x)
       (let [t (first x)]
         (or (keyword? t) (symbol? t)))))

(defn- nodes
  "Normalize a content value into a seq of nodes.
  - nil => []
  - hiccup element vector ([:div ...]) => [that-vector]
  - vector of nodes ([[:p ...] [:p ...]]) => itself
  - sequential (list/seq from for) => itself
  - scalar/string => [scalar]"
  [x]
  (cond
    (nil? x) []
    (hiccup-element? x) [x]
    (and (vector? x) (not (hiccup-element? x))) x
    (and (sequential? x) (not (string? x))) x
    :else [x]))


(defn- el
  "Construct a Hiccup element with base attrs, user attrs, and children."
  [tag base-attrs attrs children]
  (into [tag (merge-attrs base-attrs attrs)]
        (normalize-children children)))



(defn- split-opts
  "Split an opts map into {:props ... :class ... :attrs ...}.

  Conventions:
  - :class is extra classes for the root element
  - :attrs is raw hiccup attrs for the root element
  - everything else in opts is treated as component props"
  [opts]
  {:props (dissoc opts :class :attrs)
   :class (:class opts)
   :attrs (:attrs opts)})

(defn card-title
  "Card title subcomponent: emits <h2>."
  [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))]
      (el :h2 {:class class} attrs (nodes (:text props))))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :h2 {:class class} attrs children))))

(defn card-description
  "Card description subcomponent: emits <p>."
  [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))]
      (el :p {:class class} attrs (nodes (:text props))))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :p {:class class} attrs children))))

(defn card-header
  "Card header subcomponent: emits <header>."
  [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))
          {:keys [title description children]} props]
      (el :header
          {:class class}
          attrs
          [(when title (card-title {} title))
           (when description (card-description {} description))
           (nodes children)]))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :header {:class class} attrs children))))


(defn card-content
  "Card content subcomponent: emits <section>."
  [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))]
      (el :section {:class class} attrs (nodes (:children props))))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :section {:class class} attrs children))))

(defn card-footer
  "Card footer subcomponent: emits <footer>."
  [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))]
      (el :footer {:class class} attrs (nodes (:children props))))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :footer {:class class} attrs children))))


;; ---------------------------------------------------------------------------
;; Card (Basecoat: .card with > header/section/footer)
;; Source of truth: subcomponents.
;; Short form: (card {:title ... :description ... :content ... :footer ...})
;; ---------------------------------------------------------------------------


(defn card
  "Long form:
    (card {:class ... :attrs ...} children...)

  Short form (map-only):
    (card {:class ... :attrs ...
           :header <node>
           :title <node>
           :description <node>
           :content <node|seq>
           :footer <node|seq>})

  If :header is supplied, it is used verbatim. Otherwise header is synthesized
  from :title and :description."
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
      (el :div
          {:class (class-names "card" class)}
          attrs
          [header-node content-node footer-node]))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :div
          {:class (class-names "card" class)}
          attrs
          children))))
;; ---------------------------------------------------------------------------
;; Accordion (Basecoat Collapsible uses native <details>/<summary>)
;; Source of truth: accordion-item / accordion-trigger / accordion-content.
;; Short form: (accordion {:items [...]})
;; ---------------------------------------------------------------------------


(defn accordion-content
  "Accordion content: emits <section> (inside <details>)."
  [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))]
      (el :section {:class class} attrs (nodes (:children props))))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :section {:class class} attrs children))))

(defn accordion-trigger
  "Accordion trigger: emits <summary>."
  [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))
          text (:text props)]
      (el :summary {:class class} attrs (nodes text)))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :summary {:class class} attrs children))))

(defn accordion-item
  "Accordion item: emits <details> (styled by Basecoat Collapsible).
  Long form:
    (accordion-item {:open? true :attrs {...}} (accordion-trigger ...) (accordion-content ...))

  Short form (map-only):
    (accordion-item {:open? ... :title ... :content ...})"
  [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))
          {:keys [open? title content]} props]
      (el :details
          {:class class
           :open (when open? true)}
          attrs
          [(accordion-trigger {} title)
           (apply accordion-content {} (nodes content))]))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [props class attrs]} (split-opts opts)
          open? (:open? props)
          children (if (map? (first args)) children args)]
      (el :details
          {:class class
           :open (when open? true)}
          attrs
          children))))

(defn accordion
  "Long form:
    (accordion {:class ... :attrs ...} accordion-items...)

  Short form (map-only):
    (accordion {:class ... :attrs ...
                :items [{:title <node> :content <node|seq> :open? boolean
                         :item-attrs {...} :trigger-attrs {...} :content-attrs {...}}
                        ...]})

  Note: Basecoat styles Collapsible via native details/summary selectors, so
  this is intentionally minimal markup."
  [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))
          items (:items props)]
      (el :div {:class class} attrs
          (for [{:keys [title content open? item-attrs trigger-attrs content-attrs]} items]
            (accordion-item
             {:open? open?
              :attrs item-attrs}
             (accordion-trigger {:attrs trigger-attrs} title)
             (apply accordion-content {:attrs content-attrs} (nodes content))))))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :div {:class class} attrs children))))

;; ---------------------------------------------------------------------------
;; Button (Basecoat: .btn*, compound classes by variant+size)
;; Source of truth: button
;; Short form: (button {:text ... :variant ... :size ... :attrs ... :class ...})
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
    (button {:class ... :attrs ...
             :variant :primary|:secondary|:outline|:ghost|:link|:destructive|:default
             :size :md|:sm|:lg|:icon|:sm-icon|:lg-icon
             :text <node|scalar>})

  Notes:
  - defaults: :variant :default, :size :md
  - always sets :type \"button\" unless you pass {:attrs {:type \"submit\"}}"
  [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))
          {:keys [variant size text]} props
          variant (or variant :default)
          size (or size :md)
          cls (get button-classes [variant size] "btn")
          attrs' (merge {:type "button"} attrs)]
      (el :button
          {:class (class-names cls class)}
          attrs'
          (nodes text)))
    (let [[maybe-opts & children] args
          opts (if (map? maybe-opts) maybe-opts {})
          {:keys [props class attrs]} (split-opts opts)
          {:keys [variant size]} props
          variant (or variant :default)
          size (or size :md)
          cls (get button-classes [variant size] "btn")
          children (if (map? maybe-opts) children args)
          attrs' (merge {:type "button"} attrs)]
      (el :button
          {:class (class-names cls class)}
          attrs'
          children))))
