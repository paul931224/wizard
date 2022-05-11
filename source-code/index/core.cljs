(ns index.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [reagent.session :as session]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]
   [wizard.core :as wizard]))

;; -------------------------
;; Routes


(defn app []
  [wizard/view])

;; -------------------------
;; Initialize app

(defn render-app! []
  (rdom/render [app] (.getElementById js/document "app")))

(defn start-app! []
  ;; (clerk/initialize!)
  ;; (accountant/configure-navigation!
  ;;  {:nav-handler
  ;;   (fn [path]
  ;;     (let [match (reitit/match-by-path router path)
  ;;           current-page (:name (:data  match))
  ;;           route-params (:path-params match)]
  ;;       (reagent/after-render clerk/after-render!)
  ;;       (session/put! :route {:current-page (page-for current-page)
  ;;                             :route-params route-params})
  ;;       (clerk/navigate-page! path)))
        
  ;;   :path-exists?
  ;;   (fn [path]
  ;;     (boolean (reitit/match-by-path router path)))})
  ;; (accountant/dispatch-current!)
  (render-app!))