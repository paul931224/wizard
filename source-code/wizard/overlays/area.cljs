(ns wizard.overlays.area
  (:require [re-frame.core :refer [subscribe dispatch]]
            [wizard.overlays.wrapper :as overlay-wrapper]
            [wizard.editor.grid :as grid]
            [wizard.utils :as utils]))


(defn expand-horizontal-indicator []
  [:div
   {:style {:cursor :pointer
            :position :absolute
            :bottom "0px"
            :right  "-10px"
            :background :blue
            :height "5px"
            :width  "10px"}}])

(defn expand-vertical-indicator []
  [:div
   {:style {:cursor :pointer
            :position :absolute
            :top "-10px"
            :right 0
            :background :red
            :height "10px"
            :width  "5px"}}])


(defn generate-abc-matrix [how-many]
 (let [numbers (range how-many)]
  (vec (map utils/number-to-letter numbers))))



(defn randomize-rgb [a]
  (let [value-range (range 256)
        r (rand-nth value-range)
        g (rand-nth value-range)
        b (rand-nth value-range)
        a "0.5" 
        rgba-code (clojure.string/join ", " [r g b a])]
    (str "rgba(" rgba-code ")")))

(def random-colors 
 (mapv randomize-rgb (range 26)))


(defn grid-item [index item]
 [:div {:style    {:background "rgba(0,0,0,0.3)"
                   :display :flex
                   :justify-content :center
                   :align-items :center
                   :color "#DDD"
                   :height "100%"
                   :width "100%"
                   :position :relative}}                    
       [:div {:style {:background "#333"
                      :height "30px"
                      :width "30px"
                      :display :flex 
                      :justify-content :center
                      :align-items :center
                      :border-radius "15px"}}
        (str item)]])


(defn area-item [item]
  (let [letter  (fn [] (utils/number-to-letter (:position (second item))))
        active  (fn [] @(subscribe [:db/get [:overlay :active]])) 
        active? (fn [] (= (letter) (active)))]
   [:div {:on-click (fn [e] (dispatch [:db/set [:overlay :active] (letter)]))
          :style    {:background (get random-colors (:position (second item)))
                     :display :flex
                     :justify-content :center
                     :align-items :center
                     :color "#DDD"
                     :height "100%"
                     :width "100%"
                     :border (if (active?) "1px solid black" nil)
                     :position :relative
                     :grid-area (letter)}}
       [:div {:style {:background "#333"
                      :height "30px"
                      :width "30px"
                      :display :flex 
                      :justify-content :center
                      :align-items :center
                      :border-radius "15px"}}
        (str (letter))]
       (if (active?) 
         [:<> 
          [expand-horizontal-indicator]
          [expand-vertical-indicator]])]))
   
(defn grid-layer [value-path all-area-cells]
 [overlay-wrapper/view
  [:div#area-overlay
   {:style {:height "100%"
            :width "100%"
            :backdrop-filter "blur(1px)"
            :position :absolute
            :pointer-events :auto
            :left 0
            :z-index 2}}
   [grid/grid-wrapper
    (map-indexed (fn [index item] [grid-item index item])
                 all-area-cells)
    (vector
     (last value-path)
     @(subscribe [:db/get value-path]))]]])

(defn area-layer [value-path components grid-data]
 [overlay-wrapper/view
  [:div#area-overlay
   {:style {:height "100%"
            :width "100%"
            :backdrop-filter "blur(1px)"
            :position :absolute
            :pointer-events :auto
            :left 0
            :z-index 2}}
   [grid/grid-wrapper
    (map-indexed (fn [index item] [area-item item grid-data])
                 components)
    (vector
     (last value-path)
     @(subscribe [:db/get value-path]))]]])

(defn view []
  (let [overlay (subscribe [:db/get [:editor :overlay :type]])
        value-path            (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
        components-value-path (fn [] (vec (conj (value-path) :components)))
        grid-data             (fn [] @(subscribe [:db/get (value-path)]))
        components            (fn [] @(subscribe [:db/get (components-value-path)]))
        col-count    (fn [] (count (:cols (grid-data))))
        row-count    (fn [] (count (:rows (grid-data))))
        all-area-cells    (fn [] (flatten (:areas (grid-data))))
        items-count  (fn [] (count (components)))
        abc-matrix   (fn [] (generate-abc-matrix (items-count)))]
     (if (= :area @overlay)
      [:<>         
        ;[grid-layer (value-path) (all-area-cells)]
        [area-layer (value-path) (components) (grid-data)]])))
        