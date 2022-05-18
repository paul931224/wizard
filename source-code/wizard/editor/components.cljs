(ns wizard.editor.components
  (:require  [re-frame.core :refer [dispatch subscribe]]
             [wizard.editor.config :refer [row-height col-width]]))



(defn plain [key-and-comp]
 (let [comp-state                     (second key-and-comp)
       {:keys [col row width height]}  comp-state]
   [:div {:style {:position :absolute
                  :top    (str (* row row-height) "px")
                  :left   (str (* col col-width) "px")
                  :width  (str (* width  col-width) "px")
                  :height (str (* height row-height) "px")
                  :background :white}}         
      [:div.component "Plain text"]]))

(defn navbar [key-and-comp]
 (let [comp-state                     (second key-and-comp)
       {:keys [height]}  comp-state]
   [:div {:style {:position  :relative 
                  :width     "100%" 
                  :background "rgba(0,0,0,0.3)"
                  :border-bottom-left-radius "10px"
                  :border-bottom-right-radius "10px"
                  :height    (str (* height row-height) "px")
                  :display :flex 
                  :align-items :center 
                  :padding-left "10px"}}
         
     [:h2 "Navbar"]]))       


(defn component-router [comp-state]
 (let [type (:type (second comp-state))]
  (case type 
   :plain  [plain comp-state]
   :navbar [navbar comp-state]
   [plain comp-state])))
 

(defn view []
 (let [components (subscribe [:db/get [:editor :components]])] 
   [:div {:style {:position :absolute
                  :pointer-events :none          
                  :width "100%"
                  :height "100%"}}  
     [:<> 
        (map
         (fn [comp-state] ^{:key (first comp-state)}[component-router comp-state])
         @components)]]))
            
