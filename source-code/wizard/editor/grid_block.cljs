(ns wizard.editor.grid-block
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

(defn view [comp-router tree path]
  (let [comp       (get-in tree path)
        components (:components comp)
        grid-path  (vec (butlast (butlast path)))
        grid       (get-in tree grid-path)
        areas      (:areas grid)
        
        
        {:keys [position]} comp
        letter  (utils/number-to-letter position)
        area-exists? (boolean (some (fn [area] (= letter area))
                               (flatten areas)))                     
        sorted-comps     (fn [] 
                           (sort-by
                            (fn [[key value]] (:position (second value)))
                            components))]

    (if area-exists? 
     [:div {:style {:pointer-events "auto"
                    :grid-area letter
                    :width  "100%"
                    :height "100%"
                    :display :flex
                    :justify-content :center
                    :align-items :center}}                    
      (map (fn [component]
             ^{:key (first component)} 
             [comp-router tree
              (vec (concat path [:components (first component)]))])
           (sorted-comps))])))