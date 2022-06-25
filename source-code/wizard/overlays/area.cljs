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

(defn randomize-rgb []
  (let [value-range (range 256)
        r (rand-nth value-range)
        g (rand-nth value-range)
        b (rand-nth value-range)
        a "0.5" 
        rgba-code (clojure.string/join ", " [r g b a])]
    (str "rgba(" rgba-code ")")))

(defn grid-item [item grid-data]
  [:div {:style {:background (randomize-rgb)
                    :display :flex
                    :justify-content :center
                    :align-items :center
                    :color "#DDD"
                    :height "100%"
                    :width "100%"
                    :position :relative
                    :grid-area (utils/number-to-letter (:position (second item)))}}
      [:div {:style {:background "#333"
                       :padding "0px 2px"
                       :border-radius "50%"}}
       (str (utils/number-to-letter (:position (second item))))]
      [expand-horizontal-indicator]
      [expand-vertical-indicator]])
   



(defn view []
  (let [overlay (subscribe [:db/get [:editor :overlay]])
        value-path            (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
        components-value-path (fn [] (vec (conj (value-path) :components)))
        grid-data             (fn [] @(subscribe [:db/get (value-path)]))
        components            (fn [] @(subscribe [:db/get (components-value-path)]))
        col-count    (fn [] (count (:cols (grid-data))))
        row-count    (fn [] (count (:rows (grid-data))))
        items-count  (fn [] (count (components)))
        abc-matrix   (fn [] (generate-abc-matrix (items-count)))]
     (if (= :area @overlay)
      [overlay-wrapper/view
          [:div#area-overlay
           {:style {:height "100%"
                     :width "100%"
                     :backdrop-filter "blur(1px)"
                     :position :absolute
                     :left 0
                     :z-index 2}}                     
           [grid/grid-wrapper
            (map-indexed (fn [index item] [grid-item item (grid-data)])
                      (components))
            (vector
             (last (value-path))
             @(subscribe [:db/get (value-path)]))]]])))