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
   [gesso.core :as gs :refer :all ]))

(defn- section-heading
  [title description]
  [:div {:class "text-center space-y-2"}
   [:h2 {:class "font-heading leading-heading tracking-heading text-2xl font-semibold"}
    title]
   [:p {:class "font-body leading-body text-muted-foreground"} description]])

(defn- hero-section []
  [:header {:class "mx-auto max-w-3xl text-center py-6 space-y-3"}
   [:div {:class "font-body text-sm font-medium uppercase tracking-[0.2em] text-muted-foreground"}
    "Component Demo"]
   [:h1 {:class "font-heading leading-heading tracking-heading text-4xl font-bold"}
    "Gesso Component Library"]
   [:p {:class "font-body leading-body text-muted-foreground text-base-theme md:text-lg-theme"}
    "Basecoat structures wrapped in Hiccup-friendly components, tested inside a real Biff app."]])

(defn- core-surfaces-section []
  [:section {:class "gap-section space-y-6"}
   (section-heading
    "Core Surfaces"
    "Cards, actions, statuses, and alerts — the basic building blocks for most application screens.")
   [:div {:class "grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-8"}
    (card
     {:title "User Profile"
      :description "A basic surface for profile or account information."
      :content
      [:div {:class "gap-stack space-y-3 font-body leading-body"}
       [:p "Because this card lives in a grid inside the page container, it keeps a comfortable readable width."]
       [:div {:class "flex flex-wrap gap-2"}
        (badge {:variant :secondary :text "Active"})
        (badge {:variant :outline :text "Verified"})]]
      :footer
      [:div {:class "flex gap-2"}
       (button {:variant :outline :text "Cancel"})
       (button {:variant :primary :text "Edit Profile"})]})

    (card
     {:title "Status"
      :description "Small feedback primitives for queue and workflow state."
      :content
      [:div {:class "gap-stack space-y-4 font-body leading-body"}
       [:div {:class "flex flex-wrap gap-2"}
        (badge {:variant :secondary :text "Waiting"})
        (badge {:variant :outline :text "Claimed"})
        (badge {:variant :primary :text "In Progress"})
        (badge {:variant :destructive :text "Blocked"})]
       [:p {:class "font-body leading-body text-sm text-muted-foreground"}
        "Badges work well for request lifecycles, employee dashboards, and admin views."]]
      :footer
      [:div {:class "flex gap-2"}
       (button {:variant :outline :text "Back"})
       (button {:variant :secondary :text "Save"})]})

    (card
     {:title "Alerts"
      :description "Inline messaging for success and error states."
      :content
      [:div {:class "gap-stack space-y-4"}
       (alert {:title "Saved"
               :content "Your settings were saved successfully."})
       (alert {:variant :destructive
               :title "Something went wrong"
               :content "Please check the form and try again."})]})]])

(defn- form-controls-section []
  [:section {:class "gap-section space-y-6 max-w-3xl mx-auto"}
   (section-heading
    "Form Controls"
    "A more realistic composition showing fields grouped into clear sections with stronger hierarchy.")

   (card
    {:title "Profile Form"
     :description "Testing label, field, input, textarea, select, checkbox, switch, and radio group."
     :content
     [:form {:class "gap-section space-y-8"}

      [:section {:class "gap-stack space-y-5"}
       [:div {:class "space-y-1"}
        [:h3 {:class "font-heading leading-heading tracking-heading text-lg font-semibold"}
         "Identity"]
        [:p {:class "font-body leading-body text-sm text-muted-foreground"}
         "Basic account information and authentication fields."]]

       [:div {:class "gap-stack space-y-5"}
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
          :description "Password input uses the same base input component for now."})]]

      [:hr {:class "border-border"}]

      [:section {:class "gap-stack space-y-5"}
       [:div {:class "space-y-1"}
        [:h3 {:class "font-heading leading-heading tracking-heading text-lg font-semibold"}
         "Request Details"]
        [:p {:class "font-body leading-body text-sm text-muted-foreground"}
         "Structured fields suited to Humanhelp-style requests and task flows."]]

       [:div {:class "gap-stack space-y-5"}
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
          :description "Textarea uses the Basecoat textarea styling."})]]

      [:hr {:class "border-border"}]

      [:section {:class "gap-stack space-y-5"}
       [:div {:class "space-y-1"}
        [:h3 {:class "font-heading leading-heading tracking-heading text-lg font-semibold"}
         "Preferences"]
        [:p {:class "font-body leading-body text-sm text-muted-foreground"}
         "Examples of boolean and single-choice controls for notifications and settings."]]

       [:div {:class "gap-stack space-y-4"}
        [:div {:class "flex items-center gap-3 rounded-md px-3 py-2 hover:bg-accent/40"}
         (checkbox {:id "demo-terms"
                    :name "terms"
                    :checked true})
         (label {:for "demo-terms"
                 :text "I agree to receive service updates"})]

        [:div {:class "radius-lg border-theme flex items-center justify-between rounded-lg border p-4"}
         [:div {:class "space-y-1"}
          [:div {:class "font-heading leading-heading font-medium"} "Marketing emails"]
          [:p {:class "font-body leading-body text-sm text-muted-foreground"}
           "Receive occasional updates about new features."]]
         (switch {:id "demo-marketing"
                  :name "marketing"})]

        [:div {:class "space-y-3"}
         [:div {:class "space-y-1"}
          [:div {:class "font-heading leading-heading font-medium"} "Notify me about…"]
          [:p {:class "font-body leading-body text-sm text-muted-foreground"}
           "This is still a plain radio group for now; later this would be a good candidate for radio cards or segmented controls."]]
         (radio-group
          {:name "notify"
           :orientation :vertical
           :options [{:value "all" :label "All new messages" :checked true}
                     {:value "mentions" :label "Direct messages and mentions"}
                     {:value "nothing" :label "Nothing"}]})]]]]
     :footer
     [:div {:class "flex justify-end gap-2"}
      (button {:variant :outline :text "Cancel"})
      (button {:variant :primary :text "Submit"})]})])

(defn- disclosure-patterns-section []
  [:section {:class "gap-section space-y-6 max-w-3xl mx-auto"}
   (section-heading
    "Disclosure Patterns"
    "Accordion variants showing single-open, multiple-open, and concise map-driven usage.")

   [:div {:class "gap-section space-y-6"}
    (card
     {:title "Single, not collapsible"
      :description "Good for a focused FAQ or settings group where one section should always remain open."
      :content
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
         ["Why Gesso" "Gesso wraps those patterns in ergonomic Hiccup-friendly components."]]))})

    (card
     {:title "Multiple, default open values"
      :description "Useful when several sections should remain visible at once."
      :content
      (accordion
       {:type :multiple
        :collapsible? false
        :default-values [:item-1 :item-2]}
       (fn [{:keys [id title body]}]
         {:value id
          :title title
          :content body})
       [{:id :item-1 :title "One" :body [:p "A"]}
        {:id :item-2 :title "Two" :body [:p "B"]}])})

    (card
     {:title "Simple map form"
      :description "The shortest route when you just want to supply a few items."
      :content
      (accordion
       {:items [{:title "How does the layout work?"
                 :content [:p "We use a combination of a max-width container in ui/page and a CSS grid in the app function."]
                 :open? true}
                {:title "Are these native elements?"
                 :content [:p "Yes. The accordion uses HTML details and summary, styled by Basecoat and enhanced with a little hyperscript."]
                 :open? true}
                {:title "Can I use short-form maps?"
                 :content [:p "Absolutely. Most components support a map-based short form for cleaner code."]}]})})]])

(defn- tabs-section []
  [:section {:class "gap-section space-y-6 max-w-3xl mx-auto"}
   (section-heading
    "Tabs"
    "Layered sections of content shown one at a time, with a restrained Radix-inspired presentation.")

   [:div {:class "gap-section space-y-6"}

    (card
     {:title "Basic tabs"
      :description "A simple two-panel tab set with form content."
      :content
      (tabs
       {:default-value :account
        :id "demo-tabs-basic"}

       (tabs-list
        (tabs-trigger {:value :account} "Account")
        (tabs-trigger {:value :password} "Password"))

       (tabs-content
        {:value :account}
        [:div {:class "gap-stack space-y-5"}
         [:p {:class "font-body leading-body text-sm text-muted-foreground"}
          "Make changes to your account here. Click save when you're done."]
         [:div {:class "gap-stack space-y-4"}
          (field
           {:label-text "Name"
            :for "tabs-account-name"
            :control (input {:id "tabs-account-name"
                             :value "Pedro Duarte"})})
          (field
           {:label-text "Username"
            :for "tabs-account-username"
            :control (input {:id "tabs-account-username"
                             :value "@peduarte"})})]
         [:div {:class "flex justify-end"}
          (button {:variant :primary :text "Save changes"})]])

       (tabs-content
        {:value :password}
        [:div {:class "gap-stack space-y-5"}
         [:p {:class "font-body leading-body text-sm text-muted-foreground"}
          "Change your password here. After saving, you'll be logged out."]
         [:div {:class "gap-stack space-y-4"}
          (field
           {:label-text "Current password"
            :for "tabs-current-password"
            :control (input {:id "tabs-current-password"
                             :type "password"})})
          (field
           {:label-text "New password"
            :for "tabs-new-password"
            :control (input {:id "tabs-new-password"
                             :type "password"})})
          (field
           {:label-text "Confirm password"
            :for "tabs-confirm-password"
            :control (input {:id "tabs-confirm-password"
                             :type "password"})})]
         [:div {:class "flex justify-end"}
          (button {:variant :primary :text "Change password"})]]))})

    (card
     {:title "Three tabs"
      :description "Useful for compact settings and dashboard sections."
      :content
      (tabs
       {:default-value :overview
        :id "demo-tabs-three"}

       (tabs-list
        (tabs-trigger {:value :overview} "Overview")
        (tabs-trigger {:value :activity} "Activity")
        (tabs-trigger {:value :access} "Access"))

       (tabs-content
        {:value :overview}
        [:div {:class "gap-stack space-y-3"}
         [:p {:class "font-body leading-body text-sm text-muted-foreground"}
          "Overview content can stay light and summary-oriented."]
         [:div {:class "flex flex-wrap gap-2"}
          (badge {:variant :secondary :text "Healthy"})
          (badge {:variant :outline :text "12 Members"})
          (badge {:variant :outline :text "3 Pending"})]])

       (tabs-content
        {:value :activity}
        [:div {:class "gap-stack space-y-3"}
         [:p {:class "font-body leading-body text-sm text-muted-foreground"}
          "Recent activity, audit notes, or timeline-style information fits well here."]
         (alert
          {:title "Last update"
           :content "The workspace settings were updated 2 hours ago."})])

       (tabs-content
        {:value :access}
        [:div {:class "gap-stack space-y-4"}
         [:p {:class "font-body leading-body text-sm text-muted-foreground"}
          "Permission-related controls are a natural tab use case."]
         [:div {:class "radius-lg border-theme flex items-center justify-between rounded-lg border p-4"}
          [:div {:class "space-y-1"}
           [:div {:class "font-heading leading-heading font-medium"} "Allow external invites"]
           [:p {:class "font-body leading-body text-sm text-muted-foreground"}
            "Let members invite collaborators from outside the organization."]]
          (switch {:id "tabs-allow-invites"
                   :name "allow-invites"})]]))})]])

(defn- dialogs-section []
  [:section {:class "gap-section space-y-6 max-w-3xl mx-auto"}
   (section-heading
    "Dialogs"
    "Modal surfaces for confirmations, editing flows, and focused tasks.")

   [:div {:class "gap-section space-y-6"}
    (card
     {:title "Simple short form"
      :description "A dialog assembled from the root short form."
      :content
      (dialog
       {:trigger "Open dialog"
        :title "Edit profile"
        :description "Make changes to your profile here."
        :body
        [:div {:class "gap-stack space-y-4"}
         [:p {:class "font-body leading-body"} "This is the simplest way to use the dialog component."]
         (field
          {:label-text "Display name"
           :for "dialog-display-name"
           :control (input {:id "dialog-display-name"
                            :name "dialog-display-name"
                            :placeholder "Jane Doe"})})]
        :footer
        [(dialog-close {:text "Cancel"})
         (button {:variant :primary :text "Save changes"})]})})

    (card
     {:title "More complex dialog"
      :description ""
      :content
      (dialog
       {:trigger "Edit profile"
        :title "Edit profile"
        :description "Update account details."
        :body
        [:form {:class "gap-section space-y-6"}
         (field
          {:label-text "Display name"
           :for "dialog-name"
           :control (input {:id "dialog-name"
                            :name "dialog-name"
                            :placeholder "Jane Doe"})})
         (field
          {:label-text "Email"
           :for "dialog-email"
           :control (input {:type "email"
                            :id "dialog-email"
                            :name "dialog-email"
                            :placeholder "jane@example.com"})})]
        :footer
        [(dialog-close {:text "Cancel"})
         (button {:variant :primary :text "Save"})]})})

    (card
     {:title "Composed dialog"
      :description "The lower-level pieces can also be composed directly."
      :content
      (dialog {}
              (dialog-trigger {:text "Open composed dialog"})
              (dialog-overlay)
              (dialog-content {}
                              (dialog-header
                               (dialog-title {} "Delete request")
                               (dialog-description {}
                                                   "This action cannot be undone. This will permanently remove the request."))
                              (dialog-body
                               [:div {:class "gap-stack space-y-3"}
                                [:p {:class "font-body leading-body"}
                                 "Use the composed form when more control over layout is needed."]
                                (alert {:variant :destructive
                                        :title "Warning"
                                        :content "Deleted requests cannot be recovered."})])
                              (dialog-footer
                               (dialog-close {:text "Cancel"})
                               (button {:variant :destructive
                                        :text "Delete"}))))})]])

(defn- dropdown-menus-section []
  [:section {:class "gap-section space-y-6 max-w-3xl mx-auto"}
   (section-heading
    "Dropdown Menus"
    "Compact floating menus for actions, shortcuts, and grouped command lists.")

   [:div {:class "gap-section space-y-6"}

    (card
     {:title "Simple short form"
      :description "The quickest way to render a trigger plus a few menu items."
      :content
      (dropdown-menu
       {:trigger "Open menu"
        :items [{:text "Profile"}
                {:text "Settings"}
                {:separator? true}
                {:text "Sign out"}]})})

    (card
     {:title "Composed menu"
      :description "Use the lower-level pieces for labels, separators, and right-side hints."
      :content
      (dropdown-menu {}
                     (dropdown-menu-trigger
                      {:class "btn-outline"}
                      "Actions")
                     (dropdown-menu-content {}
                                            (dropdown-menu-label {:text "Account"})
                                            (dropdown-menu-item {}
                                                                [:span "Profile"]
                                                                (dropdown-menu-right-slot {} "⌘P"))
                                            (dropdown-menu-item {}
                                                                [:span "Billing"]
                                                                (dropdown-menu-right-slot {} "⌘B"))
                                            (dropdown-menu-separator)
                                            (dropdown-menu-label {:text "Session"})
                                            (dropdown-menu-item {:disabled? true}
                                                                [:span "Switch workspace"])
                                            (dropdown-menu-item {}
                                                                [:span "Sign out"])))})]])
(defn- typography-section []
  [:section {:class "section-theme max-w-4xl mx-auto"}
   [:div {:class "space-y-2"}
    (heading {:level 2 :text "Typography"})
    (muted-text
     {:as :p
      :text "A realistic sample of heading, lead, body, muted, small, and label roles under the active typography theme."})]

   (card
    {:title "Article preview"
     :description "A more realistic hierarchy test than listing heading variants one after another."
     :content
     [:article {:class "panel-theme"}
      (page-title {:text "Human help, fast."})

      (text
       {:variant :lead
        :text "Customers should be able to request help quickly, see that someone is coming, and keep moving without friction."})

      (heading
       {:level 3
        :text "Why hierarchy matters"})

      (text
       {:variant :body
        :text "A typography system should not rely on size alone. Good hierarchy also comes from line-height, weight, contrast, and spacing. If those things are working, a page feels easier to scan even before you consciously notice why."})

      (muted-text
       {:as :p
        :text "Updated 2 hours ago · Seattle flagship · 3 staff online"})

      (text
       {:variant :small
        :text "Small text is useful for secondary metadata and compact explanatory copy."})

      [:div {:class "cluster-theme items-center"}
       (label-text {:text "Status"})
       (badge {:variant :outline :text "Active"})]]

     :footer
     [:div {:class "cluster-theme justify-end"}
      (button {:variant :outline :text "Secondary"})
      (button {:variant :primary :text "Primary"})]})])




(defn- empty-states-section []
  [:section {:class "gap-section space-y-6 max-w-3xl mx-auto"}
   (section-heading
    "Empty States"
    "Reusable placeholders for empty lists, filtered views, and waiting regions.")

   [:div {:class "gap-section space-y-6"}
    (card
     {:title "Simple empty state"
      :description "Short-form usage with an optional decorative icon, title, description, and one action."
      :content
      (empty-state
        {:icon (empty-state-icon)
        :title "No waiting requests"
        :description "New requests will appear here as customers ask for help."
        :action (button {:variant :outline :text "Refresh"})})})

    (card
     {:title "Composed empty state"
      :description "Long-form usage with explicit subcomponents and multiple actions."
      :content
      (empty-state {}
                   [:div {:class "flex items-center justify-center"
                          :style {:color "var(--muted-foreground)"}}
                    (empty-state-icon)
                    ]
                   (empty-state-title "No search results")
                   (empty-state-description
                    "Try a broader query or clear your current filters.")
                   (empty-state-actions
                    (button {:variant :outline :text "Clear filters"})
                    (button {:variant :primary :text "Create request"})))})]])

(defn- icons-section []
  [:section {:class "gap-section space-y-6 max-w-3xl mx-auto"}
   (section-heading
    "Icons"
    "Vendored Lucide SVGs plus a small wrapper for consistent sizing and accessibility.")

   [:div {:class "gap-section space-y-6"}
    (card
     {:title "Lucide icons"
      :description "Direct icon lookup from the vendored Lucide set."
      :content
      [:div {:class "cluster-theme items-center"}
       [:div {:class "flex items-center gap-inline"}
        (lucide "inbox")
        [:span "Inbox"]]
       [:div {:class "flex items-center gap-inline"}
        (lucide "search")
        [:span "Search"]]
       [:div {:class "flex items-center gap-inline"}
        (lucide "alert-triangle")
        [:span "Alert triangle"]]]})

    (card
     {:title "Icon wrapper"
      :description "Wrap arbitrary icon nodes with shared sizing."
      :content
      [:div {:class "cluster-theme items-center"}
       [:div {:class "flex items-center gap-inline"}
        (icon (lucide "inbox"))
        [:span "Default icon wrapper"]]
       [:div {:class "flex items-center gap-inline"}
        (icon {:node (lucide "search")
               :size :sm})
        [:span "Small wrapped icon"]]
       [:div {:class "flex items-center gap-inline"}
        (icon {:node (lucide "alert-triangle")
               :title "Warning"})
        [:span "Accessible titled icon"]]]})

    (card
     {:title "In context"
      :description "Icons used inside other components."
      :content
      [:div {:class "space-y-4"}
       (empty-state
        {:icon (icon (lucide "inbox"))
         :title "No messages"
         :description "When a region is empty, a simple decorative icon can help without dominating the content."
         :action (button {:variant :outline :text "Refresh"})})
       [:div {:class "flex items-center gap-inline"}
        (icon {:node (lucide "check")
               :size :sm})
        [:span "A small success-style icon inline with text"]]]})]])


(defn app [ctx]
  (ui/page
   ctx
   [:div {:class "gap-section space-y-14 font-body leading-body"}
    (hero-section)
    (core-surfaces-section)
    (form-controls-section)
    (disclosure-patterns-section)
    (dialogs-section)
    (dropdown-menus-section)
    (tabs-section)
    (typography-section)
    (empty-states-section)
    (icon-section)
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
   [:p {:class "font-body leading-body"}
    "This app was made with "
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
