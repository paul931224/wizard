(ns wizard.previews.grid
   (:require [re-frame.core :refer [subscribe dispatch]]))

(defn view []
 (let [editor (subscribe [:db/get [:editor]])
       overlay (subscribe [:db/get [:editor :overlay]])]
  (fn [] 
   (if (= :grid @overlay)
    [:div#grid-preview 
       {:style {:position :absolute
                :height "100%"
                :width "100%"
                :top 0
                :background "rgba(255,0,0,0.3)"}}
       "Grid customizer"]))))