(ns wizard.overlays.selection
 (:require 
  [re-frame.core :refer [subscribe dispatch]]
  [reagent.core :as reagent :refer [atom]]
  [wizard.overlays.wrapper :as overlay-wrapper]))


(def overlay-style {:left 0
                    :z-index 1                    
                    :border "4px solid rgba(108, 245, 39, 0.69)"
                    :margin "-4px -4px 0px -4px"
                    :height "100%"
                    :width "100%"})
                    

(defn view []
 (let [overlay-type (fn [] @(subscribe [:db/get [:overlays :type]]))] 
  (if (= :selection (overlay-type)) 
   [overlay-wrapper/view
    [:div#selection-overlay {:style overlay-style}]])))