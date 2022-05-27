(ns wizard.editor.toolbars.components.core
 (:require 
  [wizard.editor.components.block :as  block]
  [wizard.editor.components.grid  :as  grid]
  [wizard.editor.components.navbar :as navbar]
  [re-frame.core :refer [dispatch subscribe]]))
  

(defn component-block [{:keys [name] :as component-data}]
  [:div {:class ["wizard-component"]
         :on-click (fn [e] (dispatch [:editor/add! component-data]))}
     name])
                       
(defn view []
  [:div {:style {:padding "0px 5px"}}
   [component-block  (block/default)]
   [component-block  (navbar/default)]   
   [component-block  (grid/default)]])