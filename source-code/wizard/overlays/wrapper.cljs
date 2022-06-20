(ns wizard.overlays.wrapper
 (:require [re-frame.core :refer [subscribe dispatch]]
           [reagent.core :as reagent]
           [wizard.dom-utils :as dom-utils]))

(defn view [editor content]
   (let [path           (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
         id             (fn [] (last (path)))
         element        (fn [] (dom-utils/get-element-by-id (id)))
         rect-data      (atom nil)
         scroll-top     (atom (.-scrollY js/window))]        
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
       (fn [editor content]
         editor
         [:div.overlay-wrapper {:style (merge {:position :absolute} @rect-data)}
          content])})))