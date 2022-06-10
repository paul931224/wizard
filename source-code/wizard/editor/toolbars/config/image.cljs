(ns wizard.editor.toolbars.config.image
 (:require [re-frame.core :refer [dispatch subscribe]]))



(defn source-input [path the-key]
  (let [new-path (vec (conj path the-key))]
    [:div
      [:div (str the-key)]
     [:input {:value @(subscribe [:db/get new-path])
              :on-change (fn [e] (dispatch [:db/set new-path (-> e .-target .-value)]))}]]))


(defn view [path]
 [source-input path :image-source])