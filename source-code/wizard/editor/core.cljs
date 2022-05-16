(ns wizard.editor.core
 (:require  [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]]))


(defn editor-particle [column-index row-index]
 [:div {:style {:width "9px" 
                :height "9px" 
                :border "1px solid white"}}])
                

(defn editor-wrapper [content]
 [:div {:style {:display :flex :justify-content :center}} 
  [:div#editor {:style {:max-width "1200px" :display :flex :flex-wrap :wrap}}
   content]])

(defn view [] 
 (let [width-particles   (range 120)
       height-particles  (range 240)]
   [editor-wrapper 
     (map 
      (fn [column-index]
        (map (fn [row-index] [editor-particle column-index row-index])
             height-particles))
      width-particles)]))