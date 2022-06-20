(ns wizard.overlays.selection
 (:require 
  [re-frame.core :refer [subscribe dispatch]]
  [reagent.core :as reagent :refer [atom]]
  [wizard.overlays.wrapper :as overlay-wrapper]))


(def overlay-style {:position :absolute
                    :left 0
                    :z-index 1
                    :pointer-events :none
                    :border "4px solid rgba(108, 245, 39, 0.69)"
                    :margin "-4px -4px 0px -4px"
                    :height "100%"
                    :width "100%"})

(defn view [editor]
 [overlay-wrapper/view
  editor 
  [:div#selection-overlay {:style overlay-style}]])