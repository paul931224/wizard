(ns wizard.overlays.block-editor
 (:require 
   [re-frame.core :refer [subscribe dispatch reg-event-fx]]
   [wizard.rich-text-editor.core :as rte]))
  

(reg-event-fx
 :rich-text-editor/open!
 (fn [{:keys [db]} [_ value-path]]
   {:dispatch-n [[:db/set [:rich-text-editor :value-path] value-path]
                 [:animation/open-rte-modal!]]}))


(reg-event-fx
 :rich-text-editor/close!
 (fn [{:keys [db]} [_ value-path]]
   {:dispatch-n [[:animation/close-rte-modal!]]}))
                 ;[:db/set [:rich-text-editor :value-path] nil]]}))  



(defn view []
  (let [value-path (subscribe [:db/get [:editor :selected :value-path]])
        content-path (fn [] (vec (concat @value-path [:content])))]
    
    [:div#rte-modal {:style {:position :fixed
                             :z-index 2000
                             :right "-100%"
                             :bottom 0
                             :width "100%" 
                             :pointer-events :none}}
     [:div
      {:style {:display :flex
               :justify-content :right
               :height "100vh"
               :flex-direction :row}} 
               
      [:div {:style {:flex-grow 1}}] 
                     
             ;:on-click #(dispatch [:rich-text-editor/close!])}]
      [:div {:style {:width "400px"
                     :height "100%" 
                     :background :white :pointer-events :auto}}
       ^{:key (content-path)}[rte/view {:value-path (content-path)}]]]]))



