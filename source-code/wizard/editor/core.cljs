(ns wizard.editor.core
 (:require  [re-frame.core :refer [dispatch subscribe]]
            [wizard.editor.components.view :as components]
            [wizard.editor.toolbars.view :as toolbars]
            [wizard.editor.events]
            [wizard.editor.config :refer [row-height col-width]]))



(defn selected-particle []
 (let [pos (subscribe [:db/get [:editor :selected-particle]])] 
  [:div#selected-particle 
    {:style {:position :absolute
             :width  (str col-width  "px")
             :height (str row-height "px")
             :background "red"
             :left   (str (* col-width  (:col @pos))
                          "px")
             :top    (str (* row-height (:row @pos)) 
                      "px")}}]))

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
 (let [editor (subscribe [:db/get [:editor :components]])] 
  [:div (str @editor)]))  

(defn page-wrapper [content]
 [:div {:style {:display :flex :justify-content :center :margin-top "60px"}}
   content])
 
(defn view [] 
 [page-wrapper 
  [:<> 
     [toolbars/view]
     [components/view]]])
        
               
        
        