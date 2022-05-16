(ns wizard.editor.sidebar
  (:require  [re-frame.core :refer [dispatch subscribe]]))


(defn view []
 [:div {:style {:position :fixed 
                :top 0 
                :right 0 
                :z-index 100 
                :color :white
                :width "300px"
                :height "100vh" 
                :background "#333"}} 
   "I am a sidebar"])