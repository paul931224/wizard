(ns wizard.overlays.grid
   (:require [re-frame.core :refer [subscribe dispatch]]
             [wizard.overlays.wrapper :as overlay-wrapper]
             [wizard.editor.grid :as grid]
             [wizard.utils :as utils]))


(defn row-indicator? [col-count index]
  (= 0 (rem (inc index) col-count)))

(defn col-indicator? [col-count index]
 (< index col-count))

(defn row-indicator []
  [:div
   {:style {:cursor :pointer 
            :position :absolute
            :bottom "0px"
            :right  "-10px"
            :background :blue
            :height "5px"
            :width  "10px"}}])

(defn col-indicator []
 [:div
  {:style {:cursor :pointer
           :position :absolute
           :top "-10px"
           :right 0
           :background :red
           :height "10px"
           :width  "5px"}}])

(defn add-col-indicator []
  [:div
   {:style {:cursor :pointer
            :position :absolute
            :top "-50px"
            :right 0
            :background :lightgreen
            :color :black
            :padding :5px
            :border-radius :5px}}                    
   "+"])

(defn add-row-indicator []
  [:div
   {:style {:cursor :pointer
            :position :absolute
            :bottom "0px"
            :right  "-50px"
            :background :lightgreen
            :color :black
            :padding :5px
            :border-radius :5px}}         
   "+"])                     

(defn grid-item [index grid-data]
 (let [col-count (count (:cols grid-data))
       row-count (count (:rows grid-data))
       add-col-index (dec col-count)
       add-row-index (dec (* col-count row-count))] 
  [:div {:style {:background "rgba(0,0,0,0.3)"
                 :display :flex
                 :justify-content :center
                 :align-items :center
                 :color "#DDD"
                 :height "100%"
                 :width "100%"
                 :position :relative}}
   [:div {:style {:background "#333"
                  :padding "0px 2px"
                  :border-radius "50%"}}
    (str (utils/number-to-letter index))]
   (if (col-indicator? col-count index)
    [col-indicator])
   (if (row-indicator? col-count index)
     [row-indicator])
   (if (= add-col-index index)
    [add-col-indicator])
   (if (= add-row-index index)
    [add-row-indicator])]))

  

(defn view []
 (let [overlay (subscribe [:db/get [:editor :overlay]])
       value-path            (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
       components-value-path (fn [] (vec (conj (value-path) :components)))
       grid-data             (fn [] @(subscribe [:db/get (value-path)]))  
       components            (fn [] @(subscribe [:db/get (components-value-path)]))     
       col-count (fn [] (count (:cols (grid-data))))
       row-count (fn [] (count (:rows (grid-data))))
       items     (fn [] (components))]
  (if (= :grid @overlay)
   [overlay-wrapper/view 
    [:div#grid-overlay
       {:style {:height "100%"
                :width "100%"   
                :position :absolute
                :left 0
                :z-index 2                      
                :background "rgba(0,255,0,0.3)"}}
       [grid/grid-wrapper
            (map-indexed (fn [index item] [grid-item index (grid-data)])
                         (items))
            (vector 
                   (last (value-path)) 
                   @(subscribe [:db/get (value-path)]))]]])))