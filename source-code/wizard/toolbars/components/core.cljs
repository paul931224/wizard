(ns wizard.toolbars.components.core
 (:require 
  [wizard.editor.components.block :as  block]
  [wizard.editor.components.grid  :as  grid]
  [wizard.editor.components.navbar :as navbar]
  [re-frame.core :refer [dispatch subscribe]]))
  

(defn component-block [component-fn]
  [:div {:class ["wizard-component"]
         :on-click (fn [e] (dispatch [:editor/add! (component-fn)]))}
     (:name (component-fn))])
                       
(defn view []
  [:div {:style {:padding "0px 5px"}}
   [component-block  (fn [] (block/default))]
   [component-block  (fn [] (navbar/default))]   
   [component-block  (fn [] (grid/default))]])