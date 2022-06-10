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


(defn unselect-component! []
  (let [selected-path      (subscribe [:editor/get-selected-component-path])]
     [:div.overlay-button {:on-click  (fn [e] (dispatch [:editor/unselect-component! @selected-path]))}
      "!"]))

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

(defn overlay-menu []
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
         [:div#overlay {:style (merge overlay-style @rect-data)}])})))
                                 
               
       
(defn view []
   [:<> 
    [overlay-menu]
    [overlay-refresher @(subscribe [:db/get [:editor]])]])                  

                    
                             