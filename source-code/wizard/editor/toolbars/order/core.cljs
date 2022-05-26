(ns wizard.editor.toolbars.order.core
 (:require [re-frame.core :refer [dispatch subscribe]]
           [plugins.drag-and-drop :as dnd]))

(defn component-hierarchy [component-data path]
  (let [path-depth (count path)]
    [:div {:style {:margin-left (str (* path-depth 10) "px")}}
     [dnd/view {:value-path (vec (concat [:editor] (conj path :components)))
                :component-data component-data
                :component      component-hierarchy}]]))

(defn view []
 (let [editor (subscribe [:db/get [:editor]])]
  (fn [] 
   [component-hierarchy @editor []])))
     
                
