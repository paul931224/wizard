(ns wizard.editor.components.view
 (:require 
  [wizard.editor.components.navbar :as navbar]
  [wizard.editor.components.block  :as block]
  [wizard.editor.components.grid   :as grid]
  [re-frame.core :refer [dispatch subscribe]]))

(defn component-wrapper [content]
  [:div.component-wrapper
   {:style {:cursor :pointer}}
   content])

(defn component-router [comp-state path]
  (let [type (:type (second comp-state))]
    [component-wrapper
     (case type
       "block"  [block/view  component-router comp-state path]
       "navbar" [navbar/view component-router comp-state path]
       "grid"   [grid/view   component-router comp-state path]
       [block/view component-router comp-state path])
     path]))



(defn view []
  (let [components (subscribe [:db/get [:editor :components]])]
    [:div {:style {:position :absolute
                   :pointer-events :none
                   :width "100%"
                   :height "100%"}}
     [:<>
      (map
       (fn [comp-state] ^{:key (first comp-state)}
         [component-router
          comp-state
          [:editor :components (first comp-state)]])
       @components)]]))