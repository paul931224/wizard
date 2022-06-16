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
 (let [editor  (subscribe [:db/get [:editor]])
       overlay (subscribe [:db/get [:editor :overlay]])]
  (fn [] 
   (if (= :order @overlay) 
    [:div {:style {:position :absolute 
                   :height "100%"
                   :width "100%"
                   :top 0
                   :background "red"}} ;:display :none}}
     [component-hierarchy @editor [:editor]]]))))
    