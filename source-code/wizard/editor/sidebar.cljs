(ns wizard.editor.sidebar
  (:require  [re-frame.core :refer [dispatch subscribe]]))


(defn view []
 [:div#sidebar-container 
   {:on-click (fn [e] (dispatch [:animation/close-sidebar!]))
    :style {:position :fixed 
            :top 0 
            :cursor :pointer
            :left 0 
            :height "100vh"
            :width  "100vw"
            :display :none 
            :z-index 95}} 
   [:div#sidebar {:style {:position :fixed 
                          :top 0 
                          :right "-100%" 
                          :z-index 100 
                          :color :white
                          :width "300px"
                          :height "100vh" 
                          :background "#333" 
                          :padding "10px"}} 
     [:h2 "I am a sidebar"]]])