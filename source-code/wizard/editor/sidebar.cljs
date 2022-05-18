(ns wizard.editor.sidebar
  (:require  [re-frame.core :refer [dispatch subscribe]]))




(defn component-block [{:keys [name] :as component-data}]
 (let [pos (subscribe [:db/get [:editor :selected-particle]])]
  [:div.wizard-component 
    {:on-click (fn [e] (dispatch [:editor/add! (merge component-data @pos)]))
     :style {:color "#333" 
             :cursor :pointer
             :padding "5px"
             :border-radius "5px"
             :background "white"}}
    [:h3 name]]))
    

(defn sidebar []
 (let [editor (subscribe [:db/get [:editor]])]
  [:div 
   [:h2 "Choose component"]
   [component-block {:type :plain 
                     :name "Plain"
                     :width 10 
                     :height 10}]]))
   

(defn view []
 [:div 
  [:div#sidebar-container 
    {:on-click (fn [e] (dispatch [:animation/close-sidebar!]))
     :style {:position :fixed 
             :top 0 
             :cursor :pointer
             :left 0 
             :height "100vh"
             :width  "100vw"
             :display :none 
             :z-index 95}}] 
  [:div#sidebar {:style {:position :fixed 
                         :top 0 
                         :right "-100%" 
                         :z-index 100 
                         :color :white
                         :width "300px"
                         :height "100vh" 
                         :background "#333" 
                         :padding "10px"}} 
    [sidebar]]])