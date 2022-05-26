(ns wizard.editor.components.navbar 
 (:require 
  [wizard.editor.config :refer [row-height col-width]]))


(defn view [comp-router key-and-comp path]
  (let [comp-state                     (second key-and-comp)
        {:keys [height]}  comp-state]
    [:div {:style {:position  :relative
                   :width     "100%"
                   :background "rgba(0,0,0,0.3)"
                   :border-bottom-left-radius "10px"
                   :border-bottom-right-radius "10px"
                   :height    (str (* height row-height) "px")
                   :display :flex
                   :align-items :center
                   :pointer-events "auto"}}
     [:h2 {:style {:padding-left (str (* 1 col-width) "px")}}
      "Navbar"]]))