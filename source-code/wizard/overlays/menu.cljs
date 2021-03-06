(ns wizard.overlays.menu
 (:require 
  [re-frame.core :refer [subscribe dispatch]]
  [wizard.editor.block :as block]))


(defn set-overlay-type! [type type-name]
 (let [active-type (subscribe [:db/get [:overlays :type]])]
   [:div.overlay-button 
    {:style {:background (if (= type @active-type) "rgba(0,150,20,0.3)")}
     :on-click  (fn [e] (dispatch [:editor/set-overlay! type]))}
    type-name]))

(defn rem-component! []
  (let [selected-path      (subscribe [:editor/get-selected-component-path])]
     [:div.overlay-button {:on-click  (fn [e] (dispatch [:editor/remove-selected-component! @selected-path]))}
      "x"]))


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
     [:div.overlay-button {:on-click  (fn [e] (dispatch [event @selected-path (position) (block/default)]))}
      content]))

(defn view []
  [:div#overlay-menu {:style {:position :fixed
                              :pointer-events :auto 
                              :top 0
                              :right 0
                              :height "auto"
                              :width  "80px"}}
    [set-overlay-type! :area  "A"]                      
    [rem-component!]
    [add-to-component!]
    [add-around-component! :editor/add-before-selected-component! "↑ +"]
    [add-around-component! :editor/add-after-selected-component!  "↓ +"]])