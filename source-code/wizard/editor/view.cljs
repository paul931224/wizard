(ns wizard.editor.view
 (:require 
  [wizard.editor.navbar        :as navbar]
  [wizard.editor.block         :as block]
  [wizard.editor.grid          :as grid]
  [wizard.editor.grid-block    :as grid-block]
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

(defn add-to-path [path & args]
 (vec (concat path args)))

(defn component-router [comp-tree path]
  (let [id   (last path)
        value (get-in comp-tree path)
        {:keys [type position]} value]
        
    [component-wrapper
       (case type
         "block"        [block/view       component-router comp-tree path]
         "grid-block"   [grid-block/view  component-router comp-tree path]
         "navbar"       [navbar/view      component-router comp-tree path]
         "grid"         [grid/view        component-router comp-tree path]
         "image"        [image/view       component-router comp-tree path]
         "root"         [component-router comp-tree (add-to-path path :components)]
         [block/view component-router comp-tree path])
       id
       path
       type
       position]))
   

(defn view []
  (let [tree       (subscribe [:db/get []])
        components (subscribe [:db/get [:editor :components]])]
    [:div {:style {:width "100%" :position :relative}}
     (map
      (fn [this-comp] ^{:key (first this-comp)}
        [component-router
         @tree
         [:editor :components (first this-comp)]])
      @components)]))       