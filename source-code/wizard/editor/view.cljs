(ns wizard.editor.view
 (:require 
  [wizard.editor.navbar        :as navbar]
  [wizard.editor.block         :as block]
  [wizard.editor.grid          :as grid]
  [wizard.editor.placeholder   :as placeholder]
  [wizard.editor.image         :as image]
  [re-frame.core :refer [dispatch subscribe]]
  [wizard.editor.events]
  [wizard.utils :as utils]))

(defn component-wrapper [content id path type position]
  (let [hovered-component (subscribe [:db/get [:editor :hovered-component]])]
   [:div.component-wrapper
    {:on-click (fn [event] 
                (.stopPropagation event)
                (dispatch [:editor/select-component! path]))
                
     :id id            
     :class (if (= id @hovered-component)
             "component-hovered" nil)
     :style {:cursor :pointer 
             :width "100%"
             :grid-area (utils/number-to-letter position)
             :height "100%"}}            
    content]))

(defn component-router [comp-state path]
  (let [id   (first comp-state)
        type (:type (second comp-state))
        position (:position (second comp-state))]
    [component-wrapper
     (case type
       "block"        [block/view  component-router comp-state path]
       "navbar"       [navbar/view component-router comp-state path]
       "grid"         [grid/view   component-router comp-state path]
       ;"placeholder"  [grid/view   component-router comp-state path]
       "image"        [image/view  component-router comp-state path]
       [block/view component-router comp-state path])
     id
     path
     type
     position]))


(defn view []
  (let [components (subscribe [:db/get [:editor :components]])]
    [:div {:style {:width "100%" :position :relative}}
      (map
       (fn [comp-state] ^{:key (first comp-state)}
         [component-router
          comp-state
          [:editor :components (first comp-state)]])
       @components)]))     