(ns index.core
  (:require
   [reitit.ring :as reitit-ring]
   [hiccup.page :refer [include-js include-css html5]]
   [ring.middleware.defaults :refer [site-defaults wrap-defaults]]))

(def middleware
  [#(wrap-defaults % site-defaults)])

(def mount-target
  [:div#app])

(defn head []
  [:head
   [:title "Wizard"]
   [:meta {:charset "utf-8"}]
   [:link {:rel "icon" 
           :type "image/png"
           :href "/images/favicon.png"}]
   (include-css "/css/wizard.css")])

(defn loading-page []
  (html5
   (head)
   [:body {:class "body-container"}
    mount-target
    (include-js "https://cdn.jsdelivr.net/npm/@metamask/detect-provider@1.2.0/dist/index.min.js")
    (include-js "https://cdn.jsdelivr.net/npm/web3@1.7.3/lib/index.min.js")
    (include-js "/js/core/app.js")]))
        


(defn index-handler
  [_request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body (loading-page)})

(def app
  (reitit-ring/ring-handler
   (reitit-ring/router
    [["/" {:get {:handler index-handler}}]
     ["/items"
      ["" {:get {:handler index-handler}}]
      ["/:item-id" {:get {:handler index-handler
                          :parameters {:path {:item-id int?}}}}]]
     ["/about" {:get {:handler index-handler}}]])
   (reitit-ring/routes
    (reitit-ring/create-resource-handler {:path "/" :root "/public"})
    (reitit-ring/create-default-handler))
   {:middleware middleware}))