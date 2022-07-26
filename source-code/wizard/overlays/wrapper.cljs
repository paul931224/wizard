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
          element-rect   (fn [] (dom-utils/get-rect-data (dom-utils/get-element-by-id (id))))
          page-rect      (fn [] (dom-utils/get-rect-data (dom-utils/get-element-by-id "page")))
          rect-data      (atom nil)
          scroll-top     (atom (.-scrollY js/window))
          element-height (atom 0)
          get-full-height (fn [] (let [rect (page-rect)]
                                  (+
                                   50
                                   (:height rect)                                   
                                   (max 
                                     (:top rect) 
                                     (- (:top rect))))))
          reset-element-height! (fn [] (reset! element-height (get-full-height)))
          editor         (subscribe [:db/get [:editor :selected]])]    
      (reagent/create-class
       {:component-did-mount  (fn [e] 
                                (reset-element-height!)
                                (reset! rect-data (element-rect)))
        :component-did-update (fn [new-argv old-argv]                ;; reagent provides you the entire "argv", not just the "props"
                                (let [old-rect @rect-data
                                      new-rect (element-rect)]
                                      
                                  (if (or
                                      ;@editor
                                       (not= (str new-rect) (str old-rect))
                                       (not= @scroll-top    (.-scrollY js/window)))
                                    (let [rect-top     (:top new-rect)
                                          scroll-y     (.-scrollY js/window)
                                          new-rect-top (+ rect-top scroll-y)
                                          new-new-rect (assoc new-rect :top new-rect-top)]
                                      (do
                                        (reset-element-height!)
                                        (.log js/console "hello")
                                        (reset! rect-data   new-new-rect)
                                        (reset! scroll-top  (.-scrollY js/window)))))))
        :reagent-render
        (fn [content]
          @editor
          [:div {:style {:position :absolute
                         :overflow-x :hidden                 
                         :height (str @element-height "px")                         
                         :width  "100%" 
                         :pointer-events :none}}
           [:div.overlay-wrapper {:style (merge 
                                                  plus-style
                                                  {:pointer-events :none
                                                   :position :absolute} 
                                                   
                                          
                                           
                                                  @rect-data)}
                    content]])}))))