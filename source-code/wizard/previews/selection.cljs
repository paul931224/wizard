(ns wizard.previews.selection
 (:require 
  [re-frame.core :refer [subscribe dispatch]]
  [reagent.core :as reagent :refer [atom]]))


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
     (if element (.log js/console "get scroll position : " (.-scrollY js/window)))
     (if bounding-rect
      {:top            (.-top     bounding-rect)
       ;:bottom         (.-bottom  bounding-rect)
       :width          (.-width   bounding-rect)
       :height         (.-height  bounding-rect)
       :left           (.-left    bounding-rect)
       :right          (.-right   bounding-rect)}
      nil)))


(def overlay-style {:position :absolute
                    :z-index 1
                    :pointer-events :none
                    :border "4px solid rgba(108, 245, 39, 0.69)"
                    :margin "-4px -4px 0px -4px"
                    :height "100%"
                    :width "100%"})

(defn view [editor]
  (let [path           (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
        id             (fn [] (last (path)))
        element        (fn [] (get-element-by-id (id)))
        rect-data      (atom nil)
        scroll-top     (atom (.-scrollY js/window))
        editor         (subscribe [:db/get [:editor]])]
    (reagent/create-class
     {:component-did-mount  (fn [e] (reset! rect-data (get-rect-data (element))))
      :component-did-update (fn [new-argv old-argv]                ;; reagent provides you the entire "argv", not just the "props"
                              (let [old-rect @rect-data
                                    new-rect (get-rect-data (element))]
                                (if (or
                                     @editor
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
         [:div#overlay {:style (merge overlay-style @rect-data)}])})))