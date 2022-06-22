(ns wizard.overlays.grid
   (:require [re-frame.core :refer [subscribe dispatch]]
             [wizard.overlays.wrapper :as overlay-wrapper]
             [wizard.editor.components.grid :as grid]))


(defn row-indicator? [col-count index]
  (= 0 (rem (inc index) col-count)))

(defn col-indicator? [col-count index]
 (< index col-count))

(defn row-indicator []
  [:div
   {:style {:position :absolute
            :bottom "0px"
            :right  "-10px"
            :background :blue
            :height "5px"
            :width  "10px"}}])

(defn col-indicator []
 [:div
  {:style {:position :absolute
           :top "-10px"
           :right 0
           :background :red
           :height "10px"
           :width  "5px"}}])

(defn grid-item [index grid-data]
 (let [col-count (count (:cols grid-data))
       row-count (count (:rows grid-data))] 
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
    (str index " - " row-count " - " col-count)]
   (if (col-indicator? col-count index)
    [col-indicator])
   (if (row-indicator? col-count index)
     [row-indicator])]))

  

(defn view []
 (let [overlay (subscribe [:db/get [:editor :overlay]])
       value-path            (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
       components-value-path (fn [] (vec (conj (value-path) :components)))
       grid-data             (fn [] @(subscribe [:db/get (value-path)]))
       items                 (fn [] @(subscribe [:db/get (components-value-path)]))]
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