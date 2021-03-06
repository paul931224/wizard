(ns wizard.sidebar.config.core
 (:require [re-frame.core :refer [dispatch subscribe]]
           [wizard.sidebar.config.box-model       :as box-model]
           [wizard.sidebar.config.image           :as image]
           [wizard.sidebar.config.add-components  :as add-components]))           


(defn change-type []
  (let [selected-path (subscribe [:db/get [:editor :selected :value-path]])]
     [:select {:value     @(subscribe [:db/get (vec (conj @selected-path :type))])}
              :on-change (fn [e] (dispatch [:editor/change-type! @selected-path (-> e .-target .-value)]))
      [:option {:value "block"} "Block"]
      [:option {:value "image"} "Image"]
      [:option {:value "grid"}  "Grid"]
      [:option {:value "block"} "Block"]]))

(defn input-title [title]
  [:div {:style {:padding "5px 0px"
                  :font-weight :bold}}
        title])


(def input-style
  {:width "100px"
   :border "0px"
   :height "30px"})

(defn input-wrapper [the-key component]
 [:div 
   [input-title (str the-key)]
   component])
 

(defn color-input [path the-key]
  (let [new-path (vec (conj path the-key))]
     [input-wrapper the-key 
       [:input {:style input-style
                :type :color
                :value @(subscribe [:db/get new-path])
                :on-change (fn [e] (dispatch [:db/set new-path (-> e .-target .-value)]))}]]))



(defn text-input [path the-key]
 (let [new-path (vec (conj path the-key))] 
  [input-wrapper the-key
   [:input {:style input-style
            :value @(subscribe [:db/get new-path])
            :on-change (fn [e] (dispatch [:db/set new-path (-> e .-target .-value)]))}]]))
 

(defn block-customizer [path]
  [:div
   [text-input path :padding]
   [color-input path :background-color]
   [color-input path :color]])
   ;[box-model/view]])

(defn type-config [component path]
 (let [{:keys [type]} component]
  [:div {:style {:padding :5px}}
   ;[type-header type]
   ;[change-type]
   (case type
    ;"grid"  [grid-customizer/view]
    "block" [block-customizer path]
    "image" [image/view path]
    [add-components/view])]))
    
 

(defn view []
  (let [selected-path      (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
        selected-component (fn [] @(subscribe [:db/get ( selected-path)]))]       
   [type-config 
       (selected-component) 
       (selected-path)]))
     
      ;[:div (str @selected-path)]]))