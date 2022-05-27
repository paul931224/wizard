(ns wizard.editor.components.block 
 (:require 
  [re-frame.core :refer [dispatch subscribe]]
  [reagent-hickory.sweet :refer [html->hiccup]]))

(defn default []
  {:type "block"
   :name "Block"
   :width 100
   :height 100
   :content "Block text"})

(defn view [comp-router key-and-comp path]
  (let [the-key                        (first key-and-comp)
        comp-state                     (second key-and-comp)
        {:keys [content col row width height
                color background-color padding]}
        comp-state
        content-path (vec (conj path :content))]
    [:div {:style {:pointer-events "auto"
                   :color color 
                   :background-color background-color
                   :padding (str padding "px")
                   :height (str height "px")
                   :width  (str width "px")}}
     [:div.component
      [:<> (html->hiccup (str "<div>" content "</div>"))]]]))