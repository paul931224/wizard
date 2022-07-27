(ns wizard.breadcrumb.view
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as reagent :refer [atom]]))

(defn one-breadcrumb [type path]
  (let [on-click (fn [] (dispatch [:db/set [:editor :selected :value-path] path]))]
     [:span.breadcrumb
      {:on-click on-click
       :style {:background "#CCC"
               :padding "5px 10px"
               :border-radius "5px"
               :cursor :pointer}}
      type])) 
      


(defn render-path [depth path]
  (let [you-are-here  (vec (take (inc depth) path))
        here          (subscribe [:db/get you-are-here])
        new-depth     (+ 2 depth)
        rest-path     (vec (drop new-depth path))
        rest-count    (count rest-path)
        type          (fn [] (str (:type @here)))]
     [:span {:style {:font-weight :bold}}
      [one-breadcrumb (type) you-are-here]
      (if (> rest-count 0)
         [:span 
          [:span {:style {:color :white}} " > "] 
          [render-path  new-depth path]])]))
      ;[:span (type)])]))


(def scroll-atom (atom 0))

(defn scroll-handler [e]
 (reset! scroll-atom (.-scrollY js/window)))

(defn view []
  (let [path      (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))]
    (reagent/create-class
      {:component-did-mount #(.addEventListener js/window "scroll" scroll-handler)
       :component-will-unmount #(.removeEventListener js/window "scroll" scroll-handler)
       :reagent-render 
       (fn []
           [:div {:style {:height "50px"
                          :width "100%"                        
                          :display :flex
                          :top (str @scroll-atom "px")
                          :position :absolute
                          :z-index 10
                          :justify-content :center
                          :align-items :center
                          :pointer-events :none}}
            [:div 
             {:style {:background "#333"
                      :border-radius "10px"
                      :padding "10px 20px"
                      :pointer-events :auto}}
             (if (< 0 (count (path)))
               [render-path 0 (path)])]])})))