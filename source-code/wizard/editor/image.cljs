(ns wizard.editor.image
 (:require
  [re-frame.core :refer [dispatch subscribe]]))



(defn view [comp-router tree path]
  (let [comp-state (get-in tree path)
        {:keys [image-source]}   comp-state]
    [:div  {:style {:display :flex
                    :justify-content :center 
                    :align-items     :center}}
     [:img {:src image-source :style {:width "100%"}}]]))     