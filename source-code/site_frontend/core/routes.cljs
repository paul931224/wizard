(ns site-frontend.core.routes
  (:require  
            [x.app-core.api :as a]   
            [plugins.web3]
            [site-frontend.index.view        :as index]))
   
            
;Getting the actual website

(a/reg-event-fx
 :website/receive-data!
 (fn [{:keys [db]} [_ response-value]]
   (let [website (get response-value :wizard/index)]
     [:db/set-item! [:wizard :data] website])))

(a/reg-event-fx
 :website/request-data!
 (fn [{:keys [db]} [_]]
   [:sync/send-query!
    :website/request
    {:on-success [:website/receive-data!]
     :query      [:wizard/index]}]))


(a/reg-event-fx
 :website/load!
 {:dispatch-n
  [[:website/request-data!]
   [:web3/setup]]})
 
   
(a/reg-event-fx
 :website/load-index!
 {:dispatch-n
  [[:website/load!]
   [:ui/set-surface! ::view {:view {:content #'index/view}}]]})