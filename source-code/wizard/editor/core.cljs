(ns wizard.editor.core
 (:require  [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]]))


(defn editor-particle [col-index row-index]
 [:div 
  {:class ["editor-particle"
           (str "col-" col-index)
           (str "row-" row-index)]
   :style {:flex-grow 1
           :height "100%" 
           :border "1px solid white"}}])
                

(defn editor-wrapper [content]
 [:div {:style {:display :flex :justify-content :center}} 
  [:div#editor {:style {:max-width "1200px" :display :flex :flex-wrap :wrap}}
   content]])

(defn row-wrapper [content]
  [:div {:style {:width "100%" :display :flex 
                 :height "20px"}}
    content])    
 
(defn view [] 
 (let [width-particles   (range 60)
       height-particles  (range 240)]
   [editor-wrapper 
     (map 
      (fn [col-index]
        [row-wrapper
         (map (fn [row-index] [editor-particle col-index row-index])
              width-particles)])
      height-particles)]))