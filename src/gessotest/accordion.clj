(ns gessotest.accordion
  (:require
   [clojure.string :as str]
   [gessotest.util :refer :all]
            ))

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
