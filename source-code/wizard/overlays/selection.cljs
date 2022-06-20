(ns wizard.overlays.selection
 (:require 
  [re-frame.core :refer [subscribe dispatch]]
  [reagent.core :as reagent :refer [atom]]
  [wizard.dom-utils :as dom-utils]))


(def overlay-style {:position :absolute
                    :left 0
                    :z-index 1
                    :pointer-events :none
                    :border "4px solid rgba(108, 245, 39, 0.69)"
                    :margin "-4px -4px 0px -4px"
                    :height "100%"
                    :width "100%"})

(defn overlay-wrapper [editor content]
  (let [path           (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
        id             (fn [] (last (path)))
        element        (fn [] (dom-utils/get-element-by-id (id)))
        rect-data      (atom nil)
        scroll-top     (atom (.-scrollY js/window))
        editor         (subscribe [:db/get [:editor]])]
    (reagent/create-class
     {:component-did-mount  (fn [e] (reset! rect-data (dom-utils/get-rect-data (element))))
      :component-did-update (fn [new-argv old-argv]                ;; reagent provides you the entire "argv", not just the "props"
                              (let [old-rect @rect-data
                                    new-rect (dom-utils/get-rect-data (element))]
                                (if (or
                                     ;@editor
                                     (not= (str new-rect) (str old-rect))
                                     (not= @scroll-top    (.-scrollY js/window)))
                                 (let [rect-top     (:top new-rect)
                                       scroll-y     (.-scrollY js/window)
                                       new-rect-top (+ rect-top scroll-y)
                                       new-new-rect (assoc new-rect :top new-rect-top)]
                                   (do
                                     (reset! rect-data   new-new-rect)
                                     (reset! scroll-top  (.-scrollY js/window)))))))
      :reagent-render
       (fn [editor]
         [:div.overlay-wrapper {:style (merge {:position :absolute} @rect-data)}
          content])})))

(defn view [editor]
 [overlay-wrapper
  editor 
  [:div {:style overlay-style}]])