(ns wizard.previews.menu
 (:require 
  [re-frame.core :refer [subscribe dispatch]]
  [wizard.editor.components.block :as block]))

(defn unselect-component! []
  (let [selected-path      (subscribe [:editor/get-selected-component-path])]
    [:div.overlay-button {:on-click  (fn [e] (dispatch [:editor/unselect-component! @selected-path]))}
     "!"]))

(defn rem-component! []
  (let [selected-path      (subscribe [:editor/get-selected-component-path])]
     [:div.overlay-button {:on-click  (fn [e] (dispatch [:editor/remove-selected-component! @selected-path]))}]
     "x"))


(defn add-to-component! []
  (let [selected-component (subscribe [:editor/get-selected-component])
        selected-path      (subscribe [:editor/get-selected-component-path])]
     [:div.overlay-button
      {:on-click  (fn [e] (dispatch [:editor/add-to-selected-component! @selected-path (block/default)]))}
      "→ +"]))

(defn add-around-component! [event content]
  (let [selected-component (subscribe [:editor/get-selected-component])
        selected-path      (subscribe [:editor/get-selected-component-path])
        position           (fn [] (:position @selected-component))]
     [:div.overlay-button {:on-click  (fn [e] (dispatch [event @selected-path (position) (block/default)]))}]
     content))

(defn view []
  [:div#overlay-menu {:style {:position :fixed
                              :top 0
                               :right 0
                              :height "auto"
                              :width  "80px"}}
    [unselect-component!]
    [rem-component!]
    [add-to-component!]
    [add-around-component! :editor/add-before-selected-component! "↑ +"]
    [add-around-component! :editor/add-after-selected-component!  "↓ +"]])