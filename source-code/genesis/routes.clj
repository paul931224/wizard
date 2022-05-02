
(ns genesis.routes
  (:require
   [server-fruits.http :as http]
   [pathom.api         :as pathom]
   [x.server-core.api  :as a]
   [x.server-ui.api :as ui]
   [server-fruits.io      :as io])
  (:gen-class))

;; -- COMPONENTS --------------------------------------------------------------
;; ----------------------------------------------------------------------------

(def body-options
  {:shield           [:div#x-app-shield]
   :plugin-js-paths                                                                                                                                        
   [{:uri  "/external-js/detect-provider.min.js"}
    {:uri  "/external-js/web3.min.js"}]})

(def head-options
  {:css-paths                                                                                                                                        
   [{:uri  "/css/wizard.css"}
    {:uri  "//cdn.jsdelivr.net/npm/hack-font@3/build/web/hack-subset.css"}]})

(defn view
  [request]
  (ui/html
   (ui/head  request head-options)
   (ui/body  request body-options)))

;; -- ROUTES ------------------------------------------------------------------
;; ----------------------------------------------------------------------------

(def METHOD-NOT-ALLOWED
  #(http/html-wrap {:body    (view %)
                    :status  404}))

(def NOT-ACCEPTABLE
  #(http/html-wrap {:body    (view %)
                    :status  404}))

(def NOT-FOUND
  #(http/html-wrap {:body    (view %)
                    :status  200}))

(def ROUTES
  {:db/query                                                                                                                                        
   {:route-template  "/query"
    :post            #(http/map-wrap {:body  (pathom/process-request! %)})}})

;; -- Lifecycle events --------------------------------------------------------
;; ----------------------------------------------------------------------------

(a/reg-lifecycles!
 ::lifecycles
 {:on-server-init  {:dispatch-n  [[:router/set-default-route! :method-not-allowed  METHOD-NOT-ALLOWED]
                                  [:router/set-default-route! :not-acceptable      NOT-ACCEPTABLE]
                                  [:router/set-default-route! :not-found           NOT-FOUND]
                                  [:router/add-routes!                             ROUTES]]}})


