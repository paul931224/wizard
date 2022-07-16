(ns wizard.overlays.wrapper
 (:require [re-frame.core :refer [subscribe dispatch]]
           [reagent.core :as reagent :refer [atom]]
           [wizard.dom-utils :as dom-utils]))

(defn view 
   ([content]
    [view content {}])
   ([content plus-style] 
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
        (fn [content]
          @editor
          [:div {:style {:position :absolute
                         :overflow-x :hidden
                         :height "100%"
                         :width  "100%" 
                         :pointer-events :none}}
           [:div.overlay-wrapper {:style (merge 
                                                  plus-style
                                                  {:pointer-events :none
                                                   :position :absolute}
                                          
                                           
                                                  @rect-data)}
                    content]])}))))