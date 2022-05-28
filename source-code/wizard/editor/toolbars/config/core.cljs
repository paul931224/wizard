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

(defn type-config [component path]
 (let [{:keys [type]} component]
  [:div {:style {:padding :5px}}
   [:div {:style {:font-weight :bold 
                  :margin-bottom "10px"}} type]
   [:div 
    [text-input path :height]
    [text-input path :width]
    [text-input path :padding]
    [color-input path :background-color]
    [color-input path :color]
    [box-model/view]
    [grid-customizer/view]]]))
 

(defn view []
  (let [selected-path      (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
        selected-component (fn [] @(subscribe [:db/get ( selected-path)]))]       
   [type-config 
     (selected-component) 
     (selected-path)]))
     
      ;[:div (str @selected-path)]]))