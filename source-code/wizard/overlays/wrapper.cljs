(ns wizard.overlays.wrapper
 (:require [re-frame.core :refer [subscribe dispatch]]
           [reagent.core :as reagent :refer [atom]]
           [wizard.dom-utils :as dom-utils]))

(defn view [content]
   (let [path           (subscribe [:db/get [:editor :selected :value-path]])
         id             (fn [] (last @path))
         element-rect   (fn [] (dom-utils/get-rect-data (dom-utils/get-element-by-id (id))))
         page-rect      (fn [] (dom-utils/get-rect-data (dom-utils/get-element-by-id "page")))
         rect-data      (atom nil)        
         element-height (atom 0)
         get-full-height (fn [] (let [rect (page-rect)]
                                  (+
                                   50
                                   (:height rect)
                                   (max
                                    (:top rect)
                                    (- (:top rect))))))
         reset-element-height! (fn [] (reset! element-height (get-full-height)))]            
      (reagent/create-class
       {:component-did-mount  (fn [e] 
                                (reset-element-height!)
                                (reset! rect-data (element-rect)))
        :component-did-update (fn [new-argv old-argv]                ;; reagent provides you the entire "argv", not just the "props"
                                (let [scroll-y (.-scrollY js/window)                                      
                                      new-rect (element-rect)]
                                  (reset-element-height!)
                                  (println (.-scrollY js/window))
                                  (let [rect-top     (max (:top new-rect) (- (:top new-rect)))
                                        new-rect-top (+ rect-top scroll-y)
                                        new-new-rect (assoc new-rect :top new-rect-top)]
                                      (if  
                                       (= @rect-data new-new-rect)
                                       nil
                                       (reset! rect-data   new-new-rect)))))
                                       
        :reagent-render
        (fn [content]          
          @path
          [:div {:style {:position :absolute
                         :overflow-x :hidden                 
                         :height (str @element-height "px")                         
                         :width  "100%"
                         :pointer-events :none}}                                             
           [:div.overlay-wrapper {:style (merge                                          
                                          {:pointer-events :none
                                           :position :absolute}
                                          @rect-data)}
                    content]])})))