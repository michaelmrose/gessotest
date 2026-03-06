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
  Accepts strings, keywords, nils, and nested sequential values.
  Splits strings and removes duplicates to prevent doubled-up classes."
  [& xs]
  (->> xs
       flatten
       (remove nil?)
       (mapcat (fn [x]
                 (cond
                   (string? x) (str/split x #"\s+")
                   (keyword? x) [(name x)]
                   (sequential? x) x
                   :else [(str x)])))
       (remove str/blank?)
       distinct
       (str/join " ")))

(defn- ->value
  "Normalize an accordion item value to a string."
  [v fallback]
  (cond
    (nil? v) (str fallback)
    (keyword? v) (name v)
    (string? v) v
    :else (str v)))

(defn merge-attrs
  "Merge Hiccup attribute maps, concatenating :class instead of overwriting it."
  [& maps]
  (reduce
   (fn [acc m]
     (let [m (or m {})]
       (-> acc
           (merge (dissoc m :class))
           (assoc :class (class-names (:class acc) (:class m))))))
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
  "Normalize a content value into a seq of nodes."
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
  (let [merged-attrs (merge-attrs base-attrs attrs)
        ;; Clean up empty class strings
        final-attrs (if (str/blank? (:class merged-attrs))
                      (dissoc merged-attrs :class)
                      merged-attrs)]
    (into [tag final-attrs] (normalize-children children))))

(defn- split-opts
  "Split an opts map into {:props ... :class ... :attrs ...}."
  [opts]
  {:props (dissoc opts :class :attrs)
   :class (:class opts)
   :attrs (:attrs opts)})


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

(defn accordion-content [& args]
  (if (only-map-arg? args)
    (let [{:keys [props class attrs]} (split-opts (first args))]
      (el :section
          {:class (class-names "p-4 pt-0 text-gray-600" class)}
          attrs
          (nodes (:children props))))
    (let [[opts & children] args
          opts (if (map? opts) opts {})
          {:keys [class attrs]} (split-opts opts)
          children (if (map? (first args)) children args)]
      (el :section
          {:class (class-names "p-4 pt-0 text-gray-600" class)}
          attrs
          children))))

(defn accordion-trigger
  "Accordion trigger: emits <summary>.

  Adds:
  - decent default spacing/hover
  - a chevron that flips ▾ / ▴ on toggle (via Hyperscript)"
  [& args]
  (let [base-class "cursor-pointer font-medium p-4 hover:bg-gray-50 list-none flex justify-between items-center gap-3"]
    (if (only-map-arg? args)
      (let [{:keys [props class attrs]} (split-opts (first args))
            text (:text props)
            ;; allow opting out of chevron
            chevron? (not= false (:chevron? props))]
        (el :summary
            {:class (class-names base-class class)}
            attrs
            (if chevron?
              [[:span {:class "min-w-0"} text]
               [:span {:data-accordion-chevron true
                       :aria-hidden "true"}
                "▾"]]
              (nodes text))))
      (let [[opts & children] args
            opts (if (map? opts) opts {})
            {:keys [props class attrs]} (split-opts opts)
            chevron? (not= false (:chevron? props))
            children (if (map? (first args)) children args)]
        (el :summary
            {:class (class-names base-class class)}
            attrs
            (if chevron?
              [(into [:span {:class "min-w-0"}] (normalize-children children))
               [:span {:data-accordion-chevron true
                       :aria-hidden "true"}
                "▾"]]
              children))))))

(defn accordion-item
  "Accordion item: emits <details>.

  Map-only form:
    (accordion-item {:value \"item-1\"
                     :title \"...\"
                     :content [...]
                     :open? true|false
                     :disabled? true|false
                     :trigger-opts {...}
                     :content-opts {...}
                     :attrs {...}
                     :class \"...\"})

  Long form:
    (accordion-item {:value ... :open? ... :disabled? ... :class ... :attrs ...}
      (accordion-trigger ...)
      (accordion-content ...))"
  [& args]
  (let [base-class "border-b last:border-b-0 group"]
    (if (only-map-arg? args)
      (let [{:keys [props class attrs]} (split-opts (first args))
            {:keys [value title content open? disabled? trigger-opts content-opts]} props
            v (->value value "item")
            details-attrs (merge-attrs
                           {:class (class-names base-class (when disabled? "opacity-60 pointer-events-none") class)
                            :open (when open? true)
                            :data-accordion-value v}
                           attrs)]
        (el :details
            {}
            details-attrs
            [(accordion-trigger (merge {:chevron? true} trigger-opts) title)
             (apply accordion-content content-opts (nodes content))]))
      (let [[opts & children] args
            opts (if (map? opts) opts {})
            {:keys [props class attrs]} (split-opts opts)
            {:keys [value open? disabled?]} props
            v (->value value "item")
            details-attrs (merge-attrs
                           {:class (class-names base-class (when disabled? "opacity-60 pointer-events-none") class)
                            :open (when open? true)
                            :data-accordion-value v}
                           attrs)
            children (if (map? (first args)) children args)]
        (el :details {} details-attrs children)))))

(defn accordion
  "Accordion root.

  Supported call styles:

  1) Map-only:
     (accordion {:items [...]})

  2) Map-only with item-fn:
     (accordion {:items coll :item-fn (fn [x] {:value ... :title ... :content ...})})

  3) Function + items:
     (accordion (fn [x] {:value ... :title ... :content ...}) coll)

  4) Options + function + items:
     (accordion {:type :single :default-value \"item-2\"} (fn [x] ...) coll)

  5) Long form children:
     (accordion {:type :multiple} (accordion-item ...) ...)

  Shadcn-ish options:
    :type            :single | :multiple (default :multiple)
    :default-value   for :single
    :default-values  for :multiple
    :collapsible?    for :single (default true)

  Notes:
  - We implement :single + :collapsible? using Hyperscript on toggle.
  - We also flip the chevron ▾/▴ on toggle."
  [& args]
  (let [root-class "border rounded-lg bg-white overflow-hidden shadow-sm"]
    (cond
      ;; (accordion {:items ...})
      (only-map-arg? args)
      (let [{:keys [props class attrs]} (split-opts (first args))
            {:keys [items item-fn type default-value default-values collapsible?]} props
            type (or type :multiple)
            collapsible? (if (nil? collapsible?) true collapsible?)
            default-one (when default-value (->value default-value default-value))
            default-many (->> (or default-values []) (map #(->value % %)) (set))
            items* (cond
                    (and item-fn items) (map item-fn items)
                    :else items)
            items* (map-indexed
                    (fn [i item]
                      (let [value (->value (:value item) (str "item-" (inc i)))
                            item (assoc item :value value)]
                        (cond
                          (contains? item :open?) item
                          (= type :single) (assoc item :open? (= value default-one))
                          (= type :multiple) (assoc item :open? (contains? default-many value))
                          :else item)))
                    (or items* []))
            toggle-script
            (when (= type :single)
              (str
               "on toggle "
               "set root to closest <div[data-accordion-root]/> "
               "if me.open "
               "  for d in <details/> in root "
               "    if d != me set d.open to false end "
               "  end "
               "else "
               (when (not collapsible?)
                 "  set anyOpen to false "
                 "  for d in <details/> in root "
                 "    if d.open set anyOpen to true end "
                 "  end "
                 "  if not anyOpen set me.open to true end ")
               "end "
               "set chev to first <span[data-accordion-chevron]/> in me "
               "if chev "
               "  if me.open put '▴' into chev else put '▾' into chev end "
               "end"))
            toggle-script-multiple
            (when (= type :multiple)
              (str
               "on toggle "
               "set chev to first <span[data-accordion-chevron]/> in me "
               "if chev "
               "  if me.open put '▴' into chev else put '▾' into chev end "
               "end"))
            script (or toggle-script toggle-script-multiple)]
        (el :div
            {:class (class-names root-class class)
             :data-accordion-root true}
            attrs
            (for [item items*]
              (let [item-attrs (get-in item [:attrs])
                    item (if script
                           (assoc item :attrs (merge-attrs item-attrs {:_ script}))
                           item)]
                (accordion-item item)))))

      ;; (accordion item-fn items)
      (and (= 2 (count args))
           (fn? (first args))
           (sequential? (second args)))
      (accordion {:item-fn (first args) :items (second args)})

      ;; (accordion opts item-fn items)
      (and (= 3 (count args))
           (map? (first args))
           (fn? (second args))
           (sequential? (nth args 2)))
      (accordion (assoc (first args) :item-fn (second args) :items (nth args 2)))

      ;; Long form: (accordion opts children...)
      :else
      (let [[opts & children] args
            opts (if (map? opts) opts {})
            {:keys [props class attrs]} (split-opts opts)
            {:keys [type collapsible?]} props
            type (or type :multiple)
            collapsible? (if (nil? collapsible?) true collapsible?)
            toggle-script
            (when (= type :single)
              (str
               "on toggle "
               "set root to closest <div[data-accordion-root]/> "
               "if me.open "
               "  for d in <details/> in root "
               "    if d != me set d.open to false end "
               "  end "
               "else "
               (when (not collapsible?)
                 "  set anyOpen to false "
                 "  for d in <details/> in root "
                 "    if d.open set anyOpen to true end "
                 "  end "
                 "  if not anyOpen set me.open to true end ")
               "end "
               "set chev to first <span[data-accordion-chevron]/> in me "
               "if chev "
               "  if me.open put '▴' into chev else put '▾' into chev end "
               "end"))
            toggle-script-multiple
            (when (= type :multiple)
              (str
               "on toggle "
               "set chev to first <span[data-accordion-chevron]/> in me "
               "if chev "
               "  if me.open put '▴' into chev else put '▾' into chev end "
               "end"))
            script (or toggle-script toggle-script-multiple)
            children (if (map? (first args)) children args)
            ;; If we can, attach the toggle behavior automatically to any <details> children.
            ;; If you don't want that, pass your own :_ on details and it will win.
            children (if script
                       (map (fn [c]
                              (if (and (vector? c) (= :details (first c)))
                                (let [[tag maybe-attrs & kids] c
                                      has-attrs? (map? maybe-attrs)
                                      a (if has-attrs? maybe-attrs {})
                                      kids (if has-attrs? kids (cons maybe-attrs kids))
                                      a' (merge-attrs {:_ script} a)]
                                  (into [tag a'] kids))
                                c))
                            children)
                       children)]
        (el :div
            {:class (class-names root-class class)
             :data-accordion-root true}
            attrs
            children)))))


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
