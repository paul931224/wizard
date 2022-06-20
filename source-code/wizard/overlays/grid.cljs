(ns wizard.overlays.grid
   (:require [re-frame.core :refer [subscribe dispatch]]
             [wizard.overlays.wrapper :as overlay-wrapper]))

(defn view []
 (let [editor (subscribe [:db/get [:editor]])
       overlay (subscribe [:db/get [:editor :overlay]])]
  (fn [] 
   (if (= :grid @overlay)
    [overlay-wrapper/view 
     @editor
     [:div#grid-overlay
        {:style {:position :absolute
                 :height "100%"
                 :width "100%"
                 :top 0
                 :background "rgba(255,0,0,0.3)"}}
        "Grid customizer"]]))))