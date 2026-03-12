(ns gessotest.ui
  (:require [cheshire.core :as cheshire]
            [clojure.java.io :as io]
            [gessotest.settings :as settings]
            [com.biffweb :as biff]
            [ring.middleware.anti-forgery :as csrf]
            [ring.util.response :as ring-response]
            [rum.core :as rum]))

(defn static-path [path]
  (if-some [last-modified (some-> (io/resource (str "public" path))
                                  ring-response/resource-data
                                  :last-modified
                                  (.getTime))]
    (str path "?t=" last-modified)
    path))

(defn base [{:keys [::recaptcha] :as ctx} & body]
  (apply
   biff/base-html
   (-> ctx
       (merge #:base{:title settings/app-name
                     :lang "en-US"
                     :icon "/img/glider.png"
                     :description (str settings/app-name " Description")
                     :image "https://clojure.org/images/clojure-logo-120b.png"})
       (update :base/head
               (fn [head]
                 (concat
                  [

                   [:link {:rel "stylesheet"
                           :href (static-path "/css/main.css")}]

                   [:link {:rel "stylesheet"
                           :href "https://cdn.jsdelivr.net/npm/basecoat-css@0.3.11/dist/basecoat.cdn.min.css"}]

                   [:script {:src "https://cdn.jsdelivr.net/npm/basecoat-css@0.3.11/dist/js/all.min.js"
                             :defer true}]

                   [:script {:src (static-path "/js/main.js")
                             :defer true}]

                   ;; HTMX + extensions
                   [:script {:src "https://unpkg.com/htmx.org@2.0.7"}]
                   [:script {:src "https://unpkg.com/htmx-ext-ws@2.0.2/ws.js"}]
                   [:script {:src "https://unpkg.com/hyperscript.org@0.9.14"}]

                   ;; Optional: recaptcha
                   (when recaptcha
                     [:script {:src "https://www.google.com/recaptcha/api.js"
                               :async "async"
                               :defer "defer"}])]
                  head))))
   body))


(defn container
  "A reusable wrapper that prevents content from stretching across ultra-wide monitors.
   Use this to wrap groups of components or entire pages."
  [& children]
  (into [:div {:class "w-full max-w-4xl mx-auto px-4 sm:px-6 lg:px-8"}]
        children))

(defn page
  "The standard app layout shell.
   Ensures the footer (if any) sticks to the bottom and the main content is centered."
  [ctx & body]
  (base ctx
        ;; The Outer App Shell (Flexbox to push footer down if page is short)
        [:div {:class "min-h-screen flex flex-col bg-slate-50"}

         ;; (Optional) Header/Navbar would go here
         ;; [:header {:class "w-full bg-white shadow-sm border-b p-4"} "My App"]

         ;; The Constrained Main Content Area
         [:main {:class "flex-grow py-10"}
          (apply container body)]

         ;; (Optional) Footer would go here
         ;; [:footer {:class "text-center p-4 text-sm text-gray-500"} "© 2026"]
         ]))

(defn on-error [{:keys [status ex] :as ctx}]
  {:status status
   :headers {"content-type" "text/html"}
   :body (rum/render-static-markup
          (page
           ctx
           [:h1.text-lg.font-bold
            (if (= status 404)
              "Page not found."
              "Something went wrong.")]))})
