(ns wizard.editor.image
 (:require
  [re-frame.core :refer [dispatch subscribe]]))



(defn view [comp-router tree path]
  (let [comp-state (get-in tree path)
        {:keys [image-source]}   comp-state]
    [:div  {:style {:height "100%"
                    :width "100%"
                    :background-image (str "url(" image-source ")")
                    :background-size  "cover"}}]))
                     