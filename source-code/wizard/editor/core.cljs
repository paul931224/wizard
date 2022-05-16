(ns wizard.editor.core
 (:require  [re-frame.core :refer [dispatch subscribe]]
            [wizard.editor.sidebar :as sidebar]))


(defn editor-particle [col-index row-index]
 [:div 
   {:on-mouse-enter (fn [a] (dispatch [:db/set [:editor :hovered-particle] 
                                       {:col col-index 
                                        :row row-index}]))
    :on-mouse-down  (fn [a] 
                        (dispatch [:db/set [:editor :selected-particle] 
                                   {:col col-index 
                                    :row row-index}])
                        (dispatch [:animation/open-sidebar!]))        
    :class ["editor-particle"
            (str "col-" col-index)
            (str "row-" row-index)]
    :style {:flex-grow 1}}])
            
          
                
(defn editor-status []
 (let [editor (subscribe [:db/get [:editor]])] 
  [:div {:style {:position :fixed 
                 :top 0 
                 :right 0}}
      (str @editor)]))  

(defn editor-wrapper [content]
 [:div {:style {:display :flex :justify-content :center :margin-top "60px"}} 
  [:div#editor {:style {:max-width "1200px" :display :flex :flex-wrap :wrap}}
   content]
  [sidebar/view]
  [editor-status]])

(defn row-wrapper [content]
  [:div {:style {:width "100%" :display :flex 
                 :height "20px"}}
    content])    
 
(defn view [] 
 (let [width-particles   (range 60)
       height-particles  (range 240)]
   [editor-wrapper 
     (map 
      (fn [row-index]
        ^{:key row-index}[row-wrapper
                           (map (fn [col-index] ^{:key (str row-index "-" col-index)}
                                                [editor-particle col-index row-index])
                                width-particles)])
      height-particles)]))