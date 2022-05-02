(ns site-backend.routes
  (:require [x.server-core.api :as a]))

(def site-routes 
  (vector 
    ["/"                    :website/home-page    [:website/load-index!]]))

(defn site-route-event [[route route-key event]] 
  [:router/add-route!  route-key
    {:route-template   route
     :client-event     event}])

(defn site-route-events []
  (mapv site-route-event site-routes))

(a/reg-lifecycles!
 ::lifecycles
 {:on-server-init 
  {:dispatch-n (site-route-events)}})     
  
