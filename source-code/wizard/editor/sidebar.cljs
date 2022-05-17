(ns wizard.editor.sidebar
  (:require  [re-frame.core :refer [dispatch subscribe]]))




(defn plain-component [editor]
 (let [data {}]
  [:div.wizard-component 
    {:on-click (fn [e] (.log js/console (:selected-particle editor)))
     :style {:color "#333" 
             :cursor :pointer
             :padding "5px"
             :border-radius "5px"
             :background "white"}}
    [:h3 "Plain"]]))
    ;[:div (str editor)]]))

(defn sidebar []
 (let [editor (subscribe [:db/get [:editor]])]
  [:div 
   [:h2 "Choose component"]
   [plain-component @editor]]))
   

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