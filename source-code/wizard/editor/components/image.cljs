(ns wizard.editor.components.image
 (:require
  [re-frame.core :refer [dispatch subscribe]]))



(defn view [comp-router key-and-comp path]
  (let [the-key                                    (first key-and-comp)
        comp-state                                 (second key-and-comp)
        {:keys [image-source]}   comp-state]
    [:div  {:style {:display :flex
                    :justify-content :center 
                    :align-items     :center}}
     [:img {:src image-source :style {:width "100%"}}]]))     