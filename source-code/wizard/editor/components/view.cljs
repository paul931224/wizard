(ns wizard.editor.components.view
 (:require 
  [wizard.editor.components.navbar :as navbar]
  [wizard.editor.components.block  :as block]
  [wizard.editor.components.grid   :as grid]
  [re-frame.core :refer [dispatch subscribe]]))

(defn component-wrapper [content id path type]
  (let [hovered-component (subscribe [:db/get [:editor :hovered-component]])]
   [:div.component-wrapper
    {:on-click (fn [event] 
                (.stopPropagation event)
                (dispatch [:db/set [:editor :selected :value-path] path]))
                 
     :class (if (= id @hovered-component)
             "component-hovered" nil)
     :style {:cursor :pointer}}            
    content]))

(defn component-router [comp-state path]
  (let [id   (first comp-state)
        type (:type (second comp-state))]
    [component-wrapper
     (case type
       "block"  [block/view  component-router comp-state path]
       "navbar" [navbar/view component-router comp-state path]
       "grid"   [grid/view   component-router comp-state path]
       [block/view component-router comp-state path])
     id
     path
     type]))


(defn view []
  (let [components (subscribe [:db/get [:editor :components]])]
    [:div {:style {:width "100%"}}
      (map
       (fn [comp-state] ^{:key (first comp-state)}
         [component-router
          comp-state
          [:editor :components (first comp-state)]])
       @components)]))