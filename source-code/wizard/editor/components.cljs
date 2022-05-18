(ns wizard.editor.components
  (:require  [re-frame.core :refer [dispatch subscribe]]
             [wizard.editor.config :refer [row-height col-width]]))


(defn plain-component []
 [:div.component "Plain text"])

(defn component-wrapper [key-and-comp]
 (let [col-index (:col (second key-and-comp))
       row-index (:row (second key-and-comp))] 
   [:div {:style {:position :absolute
                  :top   (str (* row-index row-height) "px")
                  :left  (str (* col-index col-width) "px")}}
         
         [plain-component]]))

(defn view []
 (let [components (subscribe [:db/get [:editor :components]])] 
   [:<> 
      (map
       (fn [comp-state] [component-wrapper comp-state])
       @components)]))
            
