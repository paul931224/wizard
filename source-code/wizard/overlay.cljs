(ns wizard.overlay 
  (:require [re-frame.core :refer [subscribe dispatch]]))


(defn get-element-by-id [id]
 (try 
  (js/document.getElementById id)
  (catch js/Error e nil)))

(defn get-bounding-client-rect [element]
  (try
     (.getBoundingClientRect element)
     (catch js/Error e nil)))

(defn get-rect-data [element]
 (let [bounding-rect (get-bounding-client-rect element)]
  (if bounding-rect 
   {:top            (.-top     bounding-rect)
    :bottom         (.-bottom  bounding-rect)
    :width          (.-width   bounding-rect)
    :height         (.-height  bounding-rect)
    :left           (.-left    bounding-rect)
    :right          (.-right   bounding-rect)}
   nil)))


(defn view []
  (let [path           (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
        id             (fn [] (last (path)))
        element        (fn [] (get-element-by-id (id)))
        rect-data      (fn [] (get-rect-data (element)))]                
    (str @(subscribe [:db/get [:editor]]))
    [:div#overlay {:style {:z-index 1000}} 
     [:div {:on-click (fn [] (.log js/console (rect-data)))
            :style (merge 
                     {:position :absolute                     
                      ;:pointer-events :none
                      :background "rgba(108, 245, 39, 0.69)"}
                     (rect-data))}]]))   

                    
                             