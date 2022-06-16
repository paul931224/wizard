(ns wizard.previews.order
  (:require [re-frame.core :refer [dispatch subscribe]]
           [plugins.drag-and-drop :as dnd]))

(defn component-hierarchy [component-data path]
  (let [path-depth (dec (count path))]
    [:div {:style {:margin-left (str (* path-depth 10) "px")}}
     
     [dnd/view {:value-path (vec (conj path :components))
                :component-data component-data
                :component      component-hierarchy}]]))

(defn view []
 (let [editor (subscribe [:db/get [:editor]])]
  (fn [] 
   [:div {:style {:position :absolute :top 0}}
    [component-hierarchy @editor [:editor]]])))