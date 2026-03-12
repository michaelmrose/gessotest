(ns gessotest.app
  (:require
   [cheshire.core :as cheshire]
   [com.biffweb :as biff]
   [com.biffweb.experimental :as biffx]
   [gessotest.middleware :as mid]
   [gessotest.settings :as settings]
   [gessotest.ui :as ui]
   [ring.websocket :as ws]
   [rum.core :as rum]
   [tick.core :as tick]
   [gesso.core :as gs :refer :all ]
   )

  )



(defn app [ctx]
  (ui/page
   ctx
   [:div {:class "space-y-12"}

    [:header {:class "text-center py-4"}
     [:h1 {:class "text-4xl font-bold tracking-tight"} "Gesso Component Library"]
     [:p {:class "text-gray-600 mt-2"}
      "Basecoat structures powered by Tailwind layouts."]]

    [:div {:class "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"}
     (card
      {:title "User Profile"
       :description "Managing your public presence."
       :content [:p "Because this is in a grid inside the container, it won't stretch."]
       :footer (button {:variant :primary :text "Edit Profile"})})

     (card
      {:title "Status"
       :content
       [:div {:class "flex flex-wrap gap-2"}
        (badge {:variant :secondary :text "Waiting"})
        (badge {:variant :outline :text "Claimed"})
        (badge {:variant :primary :text "In Progress"})
        (badge {:variant :destructive :text "Blocked"})]
       :footer [:div {:class "flex gap-2"}
                (button {:variant :outline :text "Cancel"})
                (button {:variant :secondary :text "Save"})]})

     (card
      {:title "Alerts"
       :content
       [:div {:class "space-y-4"}
        (alert {:title "Saved"
                :content "Your settings were saved successfully."})
        (alert {:variant :destructive
                :title "Something went wrong"
                :content "Please check the form and try again."})]})]

    [:div {:class "max-w-2xl mx-auto"}
     [:h2 {:class "text-2xl font-semibold mb-6 text-center"} "Form Components"]

     (card
      {:title "Profile Form"
       :description "Testing label, field, input, textarea, select, checkbox, switch, and radio group."
       :content
       [:form {:class "space-y-6"}

        (field
         {:label-text "Display name"
          :for "demo-display-name"
          :required? true
          :control (input {:id "demo-display-name"
                           :name "display-name"
                           :placeholder "Jane Doe"
                           :required? true})
          :description "This is the public name shown to other users."})

        (field
         {:label-text "Email"
          :for "demo-email"
          :control (input {:type "email"
                           :id "demo-email"
                           :name "email"
                           :placeholder "jane@example.com"})})

        (field
         {:label-text "Password"
          :for "demo-password"
          :control (input {:type "password"
                           :id "demo-password"
                           :name "password"
                           :placeholder "Enter a password"})
          :description "Password input uses the same base input component for now."})

        (field
         {:label-text "Request category"
          :for "demo-category"
          :control (select
                    {:id "demo-category"
                     :name "category"
                     :placeholder "Choose a category"
                     :options [{:value "rides" :label "Rides"}
                               {:value "groceries" :label "Groceries"}
                               {:value "companionship" :label "Companionship"}
                               {:value "other" :label "Other"}]})
          :description "Native select for now; richer custom select can come later."})

        (field
         {:label-text "Notes"
          :for "demo-notes"
          :control (textarea {:id "demo-notes"
                              :name "notes"
                              :rows 4
                              :placeholder "Add any helpful details..."})
          :description "Textarea uses the Basecoat textarea styling."})

        [:div {:class "space-y-4"}
         [:h3 {:class "text-lg font-medium"} "Notification Preferences"]

         [:div {:class "flex items-center gap-3"}
          (checkbox {:id "demo-terms"
                     :name "terms"
                     :checked true})
          (label {:for "demo-terms"
                  :text "I agree to receive service updates"})]

         [:div {:class "flex items-center justify-between rounded-lg border p-4"}
          [:div
           [:div {:class "font-medium"} "Marketing emails"]
           [:p {:class "text-sm text-muted-foreground"}
            "Receive occasional updates about new features."]]
          (switch {:id "demo-marketing"
                   :name "marketing"})]

         [:div {:class "space-y-3"}
          [:div {:class "font-medium"} "Notify me about..."]
          (radio-group
           {:name "notify"
            :orientation :vertical
            :options [{:value "all" :label "All new messages" :checked true}
                      {:value "mentions" :label "Direct messages and mentions"}
                      {:value "nothing" :label "Nothing"}]})]]]
       :footer [:div {:class "flex gap-2"}
                (button {:variant :outline :text "Cancel"})
                (button {:variant :primary :text "Submit"})]})]

    [:div {:class "max-w-2xl mx-auto space-y-8"}
     [:h2 {:class "text-2xl font-semibold text-center"} "Accordion"]

     [:div
      [:h3 {:class "text-lg font-medium mb-3"} "Single, not collapsible"]
      (accordion
       {:type :single
        :default-index 0
        :collapsible? false}
       (fn [{:keys [id title body]}]
         {:value id
          :title title
          :content body})
       (map-indexed
        (fn [i [title body]]
          {:id (inc i)
           :title title
           :body body})
        [["How layout works" "The outer page provides the container, and the inner grid controls card sizing."]
         ["Why Basecoat" "It gives us a solid structural baseline while Tailwind handles layout."]
         ["Why Gesso" "Gesso wraps those patterns in ergonomic Hiccup-friendly components."]]))]

     [:div
      [:h3 {:class "text-lg font-medium mb-3"} "Multiple, default open values"]
      (accordion
       {:type :multiple
        :collapsible? false
        :default-values [:item-1 :item-2]}
       (fn [{:keys [id title body]}]
         {:value id
          :title title
          :content body})
       [{:id :item-1 :title "One" :body [:p "A"]}
        {:id :item-2 :title "Two" :body [:p "B"]}])]

     [:div
      [:h3 {:class "text-lg font-medium mb-3"} "Simple map form"]
      (accordion
       {:items [{:title "How does the layout work?"
                 :content "We use a combination of a max-width container in ui/page and a CSS grid in the app function."
                 :open? true}
                {:title "Are these native elements?"
                 :content "Yes. The accordion uses HTML details and summary, styled by Basecoat and enhanced with a little hyperscript."
                 :open? true}
                {:title "Can I use short-form maps?"

                 :content "Absolutely. Most components support a map-based short form for cleaner code."}]})]]]))

#_(defn app [ctx]
  (ui/page
   ctx
   [:div {:class "space-y-12"}

    [:header {:class "text-center py-4"}
     [:h1 {:class "text-4xl font-bold tracking-tight"} "Gesso Component Library"]
     [:p {:class "text-gray-600 mt-2"} "Basecoat structures powered by Tailwind layouts."]]

    [:div {:class "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"}
     (card
      {:title "User Profile"
       :description "Managing your public presence."
       :content [:p "Because this is in a grid inside the container, it won't stretch."]
       :footer (button {:variant :primary :text "Edit Profile"})})

     (card
      {:title "Settings"
       :content [:p "Another well-sized Basecoat card using a responsive grid."]
       :footer [:div {:class "flex gap-2"}
                (button {:variant :outline :text "Cancel"})
                (button {:variant :secondary :text "Save"})]})

     (card
      {:title "Button Variants"
       :content [:div {:class "grid grid-cols-2 gap-2"}
                 (button {:variant :ghost :text "Ghost"})
                 (button {:variant :destructive :text "Danger"})
                 (button {:variant :link :text "Link Style"})
                 (button {:variant :outline :size :sm :text "Small Outline"})]})]

    [:div {:class "max-w-2xl mx-auto"}
     [:h2 {:class "text-2xl font-semibold mb-6 text-center"} "Form Components"]

     (card
      {:title "Contact Preferences"
       :description "Testing label, field, input, textarea, and select."
       :content
       [:form {:class "space-y-6"}
        (field
         {:label "Display name"
          :for "demo-display-name"
          :control (input {:attrs {:id "demo-display-name"
                                   :name "display-name"
                                   :placeholder "Jane Doe"}})
          :description "This is the public name shown to other users."})

        (field
         {:label "Email"
          :for "demo-email"
          :control (input {:type "email"
                           :attrs {:id "demo-email"
                                   :name "email"
                                   :placeholder "jane@example.com"}})})

        (field
         {:label "Request category"
          :for "demo-category"
          :control (select
                    {:attrs {:id "demo-category"
                             :name "category"}
                     :options [{:value "rides" :label "Rides"}
                               {:value "groceries" :label "Groceries"}
                               {:value "companionship" :label "Companionship"}
                               {:value "other" :label "Other"}]})
          :description "A native select for now; a richer custom select can come later."})

        (field
         {:label "Notes"
          :for "demo-notes"
          :control (textarea {:attrs {:id "demo-notes"
                                      :name "notes"
                                      :rows 4
                                      :placeholder "Add any helpful details..."}})
          :description "Textarea uses the Basecoat textarea styling."})]
       :footer [:div {:class "flex gap-2"}
                (button {:variant :outline :text "Cancel"})
                (button {:variant :primary :text "Submit"})]})]

    [:div {:class "max-w-2xl mx-auto space-y-8"}
     [:h2 {:class "text-2xl font-semibold text-center"} "Accordion"]

     [:div
      [:h3 {:class "text-lg font-medium mb-3"} "Single, not collapsible, default via index"]
      (accordion
       {:type :single
        :default-index 0
        :collapsible? false}
       (fn [{:keys [id title body]}]
         {:value id
          :title title
          :content body})
       (map-indexed
        (fn [i [title body]]
          {:id (inc i)
           :title title
           :body body})
        [["How layout works" "The outer page provides the container, and the inner grid controls card sizing."]
         ["Why Basecoat" "It gives us a solid structural baseline while Tailwind handles layout."]
         ["Why Gesso" "Gesso wraps those patterns in ergonomic Hiccup-friendly components."]]))]

     [:div
      [:h3 {:class "text-lg font-medium mb-3"} "Multiple, default open values"]
      (accordion
       {:type :multiple
        :collapsible? false
        :default-values [:item-1 :item-2]}
       (fn [{:keys [id title body]}]
         {:value id
          :title title
          :content body})
       [{:id :item-1 :title "One" :body [:p "A"]}
        {:id :item-2 :title "Two" :body [:p "B"]}])]

     [:div
      [:h3 {:class "text-lg font-medium mb-3"} "Multiple, simple map form"]
      (accordion
       {:items [{:title "How does the layout work?"
                 :content "We use a combination of a max-width container in ui/page and a CSS grid in the app function."
                 :open? true}
                {:title "Are these native elements?"
                 :content "Yes. The accordion uses HTML details and summary, styled by Basecoat and enhanced with a little hyperscript."
                 :open? true}
                {:title "Can I use short-form maps?"
                 :content "Absolutely. Most components support a map-based short form for cleaner code."}]})]]]))












#_(defn app [ctx]
  (ui/page ctx
           [:div {:class "space-y-12"}

            ;; --- Header Section ---
            [:header {:class "text-center py-4"}
             [:h1 {:class "text-4xl font-bold tracking-tight"} "Gesso Component Library"]
             [:p {:class "text-gray-600 mt-2"} "Basecoat structures powered by Tailwind layouts."]]

            ;; --- Cards Grid ---
            ;; The grid prevents cards from stretching on your 28" screen.
            [:div {:class "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8"}

             ;; Card 1: Standard Profile
             (card
               {:title "User Profile"
                :description "Managing your public presence."
                :content [:p "Because this is in a grid inside the container, it won't stretch!"]
                :footer (gs/button {:variant :primary :text "Edit Profile"})})

             ;; Card 2: Settings / Actions
             ;; FIXED: Removed the outer () from the footer vector
             (card
               {:title "Settings"
                :content [:p "Another perfectly sized Basecoat card using Tailwind's max-w-md."]
                :footer [:div {:class "flex gap-2"}
                         (button {:variant :outline :text "Cancel"})
                         (button {:variant :secondary :text "Save"})]})

             ;; Card 3: Button Gallery
             (card
               {:title "Button Variants"
                :content [:div {:class "grid grid-cols-2 gap-2"}
                          (button {:variant :ghost :text "Ghost"})
                          (button {:variant :destructive :text "Danger"})
                          (button {:variant :link :text "Link Style"})
                          (button {:variant :outline :size :sm :text "Small Outline"})]})]

            ;; --- Accordion Section ---
            [:div {:class "max-w-2xl mx-auto"}
             [:.h1 "Single not collapsible from list default via default-index"]
             (accordion
               {:type :single
                :default-index 0
                :collapsible? false}
               (fn [{:keys [id title body]}]
                 {:value id
                  :title title
                  :content body})
               (map-indexed
                 (fn [i [title body]]
                   {:id (inc i)
                    :title title
                    :body body})
                 [["fuck" "jerk"]
                  ["me" "ass"]
                  ["sideways" "bitch"]]))


             [:.h1 "Multiple with default collapsible default from default-values "]
             (accordion
               {:type :multiple
                :collapsible? false
                :default-values [:item-1 :item-2]
                }
               (fn [{:keys [id title body]}]
                 {:value id
                  :title title
                  :content body})
               [{:id :item-1 :title "One" :body [:p "A"]}
                {:id :item-2 :title "Two" :body [:p "B"]}])
             [:h2 {:class "text-2xl font-semibold mb-6 text-center"} "Frequently Asked Questions"]

             [:.h1 "Multiple simple"]
             (accordion
               {:items [{:title "How does the layout work?"
                         :content "We use a combination of a max-width container in ui/page and a CSS grid here in the app function."
                         :open? true}
                        {:title "Are these native elements?"
                         :content "Yes! The accordion uses the HTML5 <details> and <summary> tags, styled by Basecoat."
                         :open? true}
                        {:title "Can I use short-form maps?"
                         :content "Absolutely. Every component here was generated using the map-based 'Short Form' for cleaner code."}]})]
            ]))



(defn set-foo [{:keys [session params] :as ctx}]
  (biffx/submit-tx ctx
                   [[:patch-docs :user {:xt/id (:uid session)
                                        :user/foo (:foo params)}]])
  {:status 303
   :headers {"location" "/app"}})






(defn bar-form [{:keys [value]}]
  (biff/form
    {:hx-post "/app/set-bar"
     :hx-swap "outerHTML"}
    [:label.block {:for "bar"} "Bar: "
     [:span.font-mono (pr-str value)]]
    [:.h-1]
    [:.flex
     [:input.w-full#bar {:type "text" :name "bar" :value value}]
     [:.w-3]
     [:button.btn {:type "submit"} "Update"]]
    [:.h-1]
    [:.text-sm.text-gray-600
     "This demonstrates updating a value with HTMX."]))

(defn set-bar [{:keys [session params] :as ctx}]
  (time (biffx/submit-tx ctx
                         [[:patch-docs :user {:xt/id (:uid session) :user/bar (:bar params)}]]))
  (biff/render (bar-form {:value (:bar params)})))

(defn message [{:msg/keys [content sent-at]}]
  [:.mt-3 {:_ "init send newMessage to #message-header"}
   [:.text-gray-600 (tick/format "dd MMM yyyy HH:mm:ss" sent-at)]
   [:div content]])

(defn notify-clients [{:keys [gessotest/chat-clients]} record]
  (when (= "msg" (:biff.xtdb/table record))
    (let [html (rum/render-static-markup
                 [:div#messages {:hx-swap-oob "afterbegin"}
                  (message record)])]
      (doseq [ws @chat-clients]
        (ws/send ws html)))))

(defn send-message [{:keys [session] :as ctx} {:keys [text]}]
  (let [{:keys [content]} (cheshire/parse-string text true)]
    (biffx/submit-tx ctx
                     [[:put-docs :msg {:xt/id (random-uuid)
                                       :msg/user (:uid session)
                                       :msg/content content
                                       :msg/sent-at (tick/zoned-date-time)}]])))

(defn chat [{:keys [biff/conn]}]
  (let [messages (biffx/q conn
                          {:select [:msg/content :msg/sent-at]
                           :from :msg
                           :where [:>= :msg/sent-at (tick/<< (tick/now)
                                                             (tick/of-minutes 10))]})]
    [:div {:hx-ext "ws" :ws-connect "/app/chat"}
     [:form.mb-0 {:ws-send true
                  :_ "on submit set value of #message to ''"}
      [:label.block {:for "message"} "Write a message"]
      [:.h-1]
      [:textarea.w-full#message {:name "content"}]
      [:.h-1]
      [:.text-sm.text-gray-600
       "Sign in with an incognito window to have a conversation with yourself."]
      [:.h-2]
      [:div [:button.btn {:type "submit"} "Send message"]]]
     [:.h-6]
     [:div#message-header
      {:_ "on newMessage put 'Messages sent in the past 10 minutes:' into me"}
      (if (empty? messages)
        "No messages yet."
        "Messages sent in the past 10 minutes:")]
     [:div#messages
      (map message (sort-by :msg/sent-at #(compare %2 %1) messages))]]))



(defn ws-handler [{:keys [gessotest/chat-clients] :as ctx}]
  {:status 101
   :headers {"upgrade" "websocket"
             "connection" "upgrade"}
   ::ws/listener {:on-open (fn [ws]
                             (swap! chat-clients conj ws))
                  :on-message (fn [ws text-message]
                                (send-message ctx {:ws ws :text text-message}))
                  :on-close (fn [ws _status-code _reason]
                              (swap! chat-clients disj ws))}})

(def about-page
  (ui/page
    {:base/title (str "About " settings/app-name)}
    [:p "This app was made with "
     [:a.link {:href "https://biffweb.com"} "Biff"] "."]))

(defn echo [{:keys [params]}]
  {:status 200
   :headers {"content-type" "application/json"}
   :body params})


(def module
  {:static {"/about/" about-page}
   :routes ["/app" {:middleware [mid/wrap-signed-in]}
            ["" {:get app}]
            ["/set-foo" {:post set-foo}]
            ["/set-bar" {:post set-bar}]
            ["/chat" {:get ws-handler}]]
   :api-routes [["/api/echo" {:post echo}]]
   :on-tx notify-clients})
