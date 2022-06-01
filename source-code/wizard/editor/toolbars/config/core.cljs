(ns wizard.editor.toolbars.config.core
 (:require [re-frame.core :refer [dispatch subscribe]]
           [wizard.editor.toolbars.config.grid-customizer :as grid-customizer]
           [wizard.editor.toolbars.config.box-model       :as box-model]))

(defn color-input [path the-key]
  (let [new-path (vec (conj path the-key))]
     [:div
      [:div (str the-key)]
      [:input {:type :color
               :value @(subscribe [:db/get new-path])
               :on-change (fn [e] (dispatch [:db/set new-path (-> e .-target .-value)]))}]]))

(defn text-input [path the-key]
 (let [new-path (vec (conj path the-key))] 
  [:div 
   [:div (str the-key)]
   [:input {:value @(subscribe [:db/get new-path])
            :on-change (fn [e] (dispatch [:db/set new-path (-> e .-target .-value)]))}]]))

(defn type-header [type]
 [:div {:style {:font-weight :bold 
                  :margin-bottom "10px"}} 
      "Type: " type])
 

(defn type-config [component path]
 (let [{:keys [type]} component]
  [:div {:style {:padding :5px}}
   [type-header type]
   (case type
    "grid"  [grid-customizer/view]
    "block" [:div 
             [text-input path :height]
             [text-input path :width]
             [text-input path :padding]
             [color-input path :background-color]
             [color-input path :color]
             [box-model/view]]
    [:div {:style {:padding "0px 5px"}} "No config for this type of component."])]))
    
 

(defn view []
  (let [selected-path      (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
        selected-component (fn [] @(subscribe [:db/get ( selected-path)]))]       
   [type-config 
     (selected-component) 
     (selected-path)]))
     
      ;[:div (str @selected-path)]]))