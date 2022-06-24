(ns wizard.editor.block 
 (:require 
  [re-frame.core :refer [dispatch subscribe]]
  [reagent-hickory.sweet :refer [html->hiccup]]
  [wizard.utils :as utils]))

(defn default []
  {:type "block"
   :name "Block"
   :width 100
   :height 100
   :content "Placeholder element"})

(defn view [comp-router key-and-comp path]
  (let [the-key                                    (first key-and-comp)
        comp-state                                 (second key-and-comp)
        {:keys [content col row width height
                color background-color padding 
                position]}   comp-state]
       
    [:div {:style {:pointer-events "auto"
                   :color color
                   :grid-area (utils/number-to-letter position) 
                   :background-color (if background-color background-color "white")
                   :width  "100%"
                   :height "100%"
                   :display :flex 
                   :justify-content :center 
                   :align-items :center}}                                     
     [:div.component {:style {:padding padding}}
      [:<> (html->hiccup (str "<div>" content "</div>"))]]]))