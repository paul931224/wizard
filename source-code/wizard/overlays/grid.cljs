(ns wizard.overlays.grid
   (:require [re-frame.core :refer [subscribe dispatch]]
             [wizard.overlays.wrapper :as overlay-wrapper]))

(defn view []
 (let [overlay (subscribe [:db/get [:editor :overlay]])]
  (if (= :grid @overlay)
   [overlay-wrapper/view 
    [:div#grid-overlay
       {:style {:height "100%"
                :width "100%"   
                :position :absolute
                :left 0
                :z-index 2                      
                :background "rgba(255,0,0,0.3)"}}
       "Grid customizer"]])))