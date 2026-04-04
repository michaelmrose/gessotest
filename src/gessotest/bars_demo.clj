(ns gessotest.bars-demo
  (:require
   [gesso.components.bars.core :as bars]
   [gesso.core :refer [button card group icon section-block status-pill text]]
   [gessotest.ui :as ui]))

(def ^:private demo-href
  "/app/bars-demo")

(defn- demo-link-item
  ([text]
   (demo-link-item text {}))
  ([text opts]
   (bars/menu-item
    (merge {:text text
            :href demo-href}
           opts))))

(defn- dashboard-menu []
  (bars/menu
   {:home-region :leftmost
    :priority 100
    :groups [(bars/menu-group
              {:items [(demo-link-item "Dashboard" {:current? true})]})]}))

(defn- browse-menu []
  (bars/menu
   {:label "Browse"
    :home-region :center
    :priority 80
    :collapse-at :small
    :groups [(bars/menu-group
              {:items [(demo-link-item "Requests")
                       (demo-link-item "Employees")
                       (demo-link-item "Reports")]})]}))

(defn- account-menu []
  (bars/menu
   {:label "Account"
    :home-region :rightmost
    :category :account
    :collapse-at :medium
    :priority 20
    :groups [(bars/menu-group
              {:heading "Session"
               :items [(demo-link-item "Profile")
                       (demo-link-item "Settings")
                       (bars/menu-item {:text "Sign out"})]})]}))

(defn- operations-menu []
  (bars/menu
   {:label "Operations"
    :home-region :sidebar
    :category :operations
    :priority 60
    :groups [(bars/menu-group
              {:heading "Queues"
               :items [(demo-link-item "Waiting")
                       (demo-link-item "Active")
                       (demo-link-item "Completed")]})
             (bars/menu-group
              {:heading "Tools"
               :items [(demo-link-item "Schedule")
                       (demo-link-item "Coverage")]})]}))

(defn- demo-menus []
  [
   ;; (dashboard-menu)
   (browse-menu)
   (account-menu)
   (operations-menu)])

(defn- demo-brand []
  [:a {:href demo-href
       :class "cluster-theme items-center"
       :style {:color "var(--foreground)"
               :text-decoration "none"}}
   (icon "search" {:size :sm})
   [:span {:class "font-heading text-md-theme weight-semibold-theme"}
    "Gesso"]])

(defn- intro-card []
  (card
   {:title "Bars demo"
    :description "This route exists only to load the current bars component in a real gessotest page."
    :content
    [:div {:class "content-stack-theme"}
     (text
      {:variant :body
       :text "Resize the browser to verify the large, medium, and small behaviors."})
     (text
      {:variant :muted
       :text "This page is deliberately plain. The point is to make structural and breakpoint bugs obvious."})]}))

(defn- behavior-card []
  (card
   {:title "What to look for"
    :description "Manual test checklist."
    :content
    [:div {:class "content-stack-theme"}
     (text
      {:variant :body
       :text "Large: topbar visible, sidebar visible, hamburger likely absent."})
     (text
      {:variant :body
       :text "Medium: sidebar collapsed, hamburger visible, opening it should push content downward."})
     (text
      {:variant :body
       :text "Small: opening the hamburger should replace the page content below the topbar."})]}))

(defn- filler-card []
  (card
   {:title "Dummy actions"
    :description "Simple filler content so layout changes are easy to notice."
    :content
    [:div {:class "content-stack-theme"}
     (button {:variant :primary :text "Primary action"})
     (button {:variant :outline :text "Secondary action"})
     (button {:variant :ghost :text "Tertiary action"})]}))

(defn- notes-block []
  (section-block
   {:title "Notes"
    :description "Current scope of this test page."
    :content
    [[:div {:class "content-stack-theme"}
      (text
       {:variant :body
        :text "This page is testing the bars component exactly as it exists now."})
      (text
       {:variant :body
        :text "It is not asserting richer dropdown behavior or measured overflow logic that has not been implemented."})]]}))

(defn- status-row []
  (group {:class "cluster-theme"}
         (status-pill {:status :active :text "Topbar"})
         (status-pill {:status :info :text "Sidebar"})
         (status-pill {:status :warning :text "Hamburger"})))

(defn- demo-content []
  [:div {:class "pad-container py-8"}
   [:div {:class "section-theme max-w-5xl mx-auto"}
    (intro-card)
    (status-row)
    [:div {:class "grid grid-cols-1 xl:grid-cols-2 gap-6"}
     (behavior-card)
     (filler-card)]
    (notes-block)]])

(defn bars-demo-page
  [ctx]
  (ui/page-shell
   ctx
   (bars/bars
    {:brand (demo-brand)
     :sidebar-collapse-at :medium
     :menus (demo-menus)}
    (demo-content))))
