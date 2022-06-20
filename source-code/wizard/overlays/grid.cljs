(ns wizard.overlays.grid
   (:require [re-frame.core :refer [subscribe dispatch]]
             [wizard.overlays.wrapper :as overlay-wrapper]
             [wizard.editor.components.grid :as grid]))


(defn grid-item [content]
 [:div {:style {:background "rgba(0,0,0,0.3)"
                :display :flex
                :justify-content :center
                :align-items :center
                :color "#DDD"
                :height "100%"
                :width "100%"
                :position :relative}}
  [:div {:style {:background "#333"
                 :padding "0px 2px"
                 :border-radius "50%"}}
   (str content)]])

(defn view []
 (let [overlay (subscribe [:db/get [:editor :overlay]])
       value-path (subscribe [:db/get [:editor :selected :value-path]])
       components-value-path (fn [] (vec (conj @value-path :components)))
       items                 (fn [] @(subscribe [:db/get (components-value-path)]))]
  (if (= :grid @overlay)
   [overlay-wrapper/view 
    [:div#grid-overlay
       {:style {:height "100%"
                :width "100%"   
                :position :absolute
                :left 0
                :z-index 2                      
                :background "rgba(0,255,0,0.3)"}}
       [grid/grid-wrapper
          (map-indexed (fn [index item] [grid-item (inc index)])
                       (items))
          (vector 
                 (last @value-path) 
                 @(subscribe [:db/get @value-path]))]]])))