(ns wizard.overlays.order
  (:require [re-frame.core :refer [dispatch subscribe]]
            [plugins.drag-and-drop :as dnd]
            [plugins.sortable-grid :as sortable-grid]
            [wizard.overlays.wrapper :as overlay-wrapper]))

(defn view []
 (let [editor  (subscribe [:db/get [:editor]])
       overlay (subscribe [:db/get [:editor :overlay]])
       selected-path (subscribe [:db/get [:editor :selected :value-path]])]
  (fn [] 
   (if (= :order @overlay) 
    [overlay-wrapper/view 
     @editor
     [:div#order-overlay {:style {:position :absolute 
                                  :height "100%"
                                  :width "100%"
                                  :top 0}} ;:display :none}}
      [sortable-grid/view {:value-path @selected-path}]]]))))
               
    