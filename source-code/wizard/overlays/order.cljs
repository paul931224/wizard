(ns wizard.overlays.order
  (:require [re-frame.core :refer [dispatch subscribe]]
            [plugins.drag-and-drop :as dnd]
            [plugins.sortable-grid :as sortable-grid]
            [wizard.overlays.wrapper :as overlay-wrapper]))





(defn view []
 (let [overlay (subscribe [:db/get [:editor :overlay]])
       selected-path (subscribe [:db/get [:editor :selected :value-path]])]
  (fn [] 
   (if (= :order @overlay) 
    [overlay-wrapper/view 
     [:div#order-overlay {:style {:position :absolute                                                        
                                  :left 0
                                  :z-index 2 
                                  :height "100%"
                                  :width "100%"}}                                  
      [sortable-grid/view {:value-path @selected-path}]]]))))
               
    