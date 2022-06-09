(ns wizard.editor.components.placeholder
 (:require
    [re-frame.core :refer [dispatch subscribe]]
    [reagent-hickory.sweet :refer [html->hiccup]]))

(defn view [comp-router key-and-comp path]
  (let [the-key                        (first key-and-comp)
        comp-state                     (second key-and-comp)
        {:keys [content col row width height
                color background-color padding]}
        comp-state
        content-path (vec (conj path :content))]
    [:div {:style {:pointer-events "auto"
                   :color color
                   :background-color (if background-color background-color "white")
                   :width  "100%"
                   :height "100%"
                   :display :flex
                   :justify-content :center
                   :align-items :center}}                  
     [:div.component {:style {:padding padding}}
      [:img {:src "/images/placeholder-icon.png"}]]]))