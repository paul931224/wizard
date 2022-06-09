(ns wizard.overlay 
  (:require [re-frame.core :refer [subscribe dispatch]]
            [reagent.core :as reagent :refer [atom]]
            [wizard.editor.components.block :as block]))


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


(def menu-button-style 
 {:width "100%"
  :height  "20px"
  :background :white
  :border-radius "5px"
  :position :absolute 
  :cursor :pointer
  :box-shadow "5px 5px 15px -4px rgba(108, 245, 39, 0.69)"
  :display :flex 
  :padding "5px 0px"
  :justify-content :center
  :pointer-events :auto})

(def menu-top-style
 (merge menu-button-style {:top "-20px"}))

(def menu-bot-style 
 (merge menu-button-style {:bottom "-20px"}))

(defn add-component! [style event]
 (let [selected-component (subscribe [:editor/get-selected-component])
       selected-path      (subscribe [:editor/get-selected-component-path])
       position           (fn [] (:position @selected-component))] 
  [:div.overlay-button {:on-click  (fn [e] (dispatch [event @selected-path (position) (block/default)]))
                        :style     style} 
    "+"]))
 

(defn overlay-menu []
 [:div#overlay-menu {:style {:position :relative 
                             :height "100%"
                             :width  "100%"}}
      [add-component! menu-top-style :editor/add-before-component!]
      [add-component! menu-bot-style :editor/add-after-component!]])
           

(def overlay-style {:position :absolute
                    :z-index 1
                    :pointer-events :none
                    :background "rgba(108, 245, 39, 0.69)"
                    :height "100%"
                    :width "100%"})

(defn overlay-refresher [editor]
  (let [path           (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
        id             (fn [] (last (path)))
        element        (fn [] (get-element-by-id (id)))
        rect-data      (atom nil)]                
    (reagent/create-class  
     {:component-did-mount  (fn [e] (reset! rect-data (get-rect-data (element))))
      :component-did-update (fn [this old]                ;; reagent provides you the entire "argv", not just the "props"
                               (let [new (first (rest (reagent/argv this)))]
                                 (reset! rect-data (get-rect-data (element)))))     
      :reagent-render
       (fn [editor] 
         [:div#overlay {:style (merge overlay-style @rect-data)}
             [overlay-menu]])})))                    
               
       
(defn view []
   [overlay-refresher @(subscribe [:db/get [:editor]])])                  

                    
                             