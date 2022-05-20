(ns wizard.editor.core
 (:require  [re-frame.core :refer [dispatch subscribe]]
            [wizard.editor.components :as components]
            [wizard.editor.sidebar :as sidebar]
            [wizard.editor.events]
            [wizard.editor.config :refer [row-height col-width]]
            [wizard.editor.rich-text-editor.core :as rtf]))



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
 [:div 
  ;[editor-status]
  [:div {:style {:display :flex :justify-content :center :margin-top "60px"}}
   content]])
 

(defn editor-wrapper [content]
 [:<> 
  [:div#editor {:style {:position :relative
                        :max-width "1200px" :display :flex :flex-wrap :wrap}}
              content]])
  
  

(defn row-wrapper [content]
  [:div {:style {:width "100%" :display :flex 
                 :height (str row-height "px")}}
    content])    
 
(defn editor-grid []
 (let [width-particles   (range 60)
       height-particles  (range 240)]
  [:div {:style {:width "100%" 
                 :display :flex
                 :flex-wrap :wrap}}
   (map
    (fn [row-index]
      ^{:key row-index} [row-wrapper
                         (map (fn [col-index] ^{:key (str row-index "-" col-index)}
                                               [editor-particle col-index row-index])
                              width-particles)])
    height-particles)])) 
 
(defn rte-modal []
 (let [value-path (subscribe [:db/get [:rich-text-editor :value-path]])]
  [:div#rte-modal {:style {:position :fixed 
                           :z-index 2000
                           :bottom "-100%"
                           :left 0
                           :width "100%"}}
    [:div 
     {:style {:display :flex :justify-content :right
              :height "100vh" 
              :flex-direction :column}}
     [:div {:style {:flex-grow 1 :width "100vw"}
            :on-click #(dispatch [:rich-text-editor/close!])}]
     [:div {:style {:background :white}}
       (if @value-path 
        [rtf/view {:value-path @value-path}])]]]))

(defn view [] 
 [page-wrapper 
    [editor-wrapper 
      [:<> 
        [rte-modal]
        [selected-particle] 
        [components/view]
        [editor-grid]
        [sidebar/view]]]])
               
        
        