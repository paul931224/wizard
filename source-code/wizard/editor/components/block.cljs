(ns wizard.editor.components.block 
 (:require 
  [re-frame.core :refer [dispatch subscribe]]
  [reagent-hickory.sweet :refer [html->hiccup]]))

(defn default []
  {:type "block"
   :name "Block"
   :width 30
   :height 10
   :content "Block text"})

(defn view [comp-router key-and-comp path]
  (let [the-key                        (first key-and-comp)
        comp-state                     (second key-and-comp)
        {:keys [content col row width height]}  comp-state
        content-path (vec (conj path :content))]
    [:div {:style {:pointer-events "auto"}}
     [:div.component
      [:<> (html->hiccup (str "<div>" content "</div>"))]]]))