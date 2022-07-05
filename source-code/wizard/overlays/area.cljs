(ns wizard.overlays.area
  (:require  [reagent.core :as r]
             [re-frame.core :refer [dispatch subscribe]]
             [wizard.overlays.wrapper :as overlay-wrapper]
             ["react" :as react]
             ["@dnd-kit/core" :refer [useDraggable useDroppable DndContext]]
             ["@dnd-kit/utilities" :refer [CSS]]
             ["@dnd-kit/modifiers" :refer [restrictToWindowEdges]]
             [wizard.editor.grid :as grid]
             [wizard.utils :as utils]
             [wizard.dom-utils :as dom-utils]))

(def dnd-context (r/adapt-react-class DndContext))

(def resize-atom (r/atom {:top 0
                          :left 0
                          :height 50
                          :width 100}))

;;
;; UTILS
;;



(defn get-overlapping-areas [this-area areas]
  (vec
   (map 
    first
    (filter
     (fn [area]
       (let [first-width  (:width  this-area)
             first-height (:height this-area)
             first-top    (:top    this-area)
             first-left   (:left   this-area)
             first-bottom (+ first-top  first-height)            
             first-right  (+ first-left first-width)
             second-width  (:width   (second area))
             second-height (:height  (second area))
             second-top    (:top     (second area))
             second-left   (:left    (second area))
             second-bottom (+ second-top  second-height)
             second-right  (+ second-left second-width)]      
         (cond 
          (> first-left  second-right)  false
          (> second-left first-right)  false
          (> first-top  second-bottom) false 
          (> second-top first-bottom)  false
          :else true)))
     areas))))


(defn set-overlapping-areas [id]
  (let [area-dropzones    (subscribe [:db/get [:overlays :areas :area-dropzones]])
        overlapping-areas (get-overlapping-areas
                           (dom-utils/get-rect-data (.getElementById js/document (str "area-" id)))
                           (mapv
                            (fn [a] [(first a) (dom-utils/get-rect-data (second a))])
                            @area-dropzones))]
    (dispatch [:db/set [:overlays :areas :overlapping-areas] overlapping-areas])))


(defn handle-drag-start [value-path]
  (fn [event] 
   (let [{:keys [active over]} (utils/to-clj-map event)
         id      (:id active)]
     (dispatch [:db/set [:overlays :areas :dragged] id]))))

(defn handle-drag-move [value-path]
  (fn [event]
    (let [{:keys [active over]} (utils/to-clj-map event)
          id      (:id active)     
          overlapping-positions  (subscribe [:db/get [:overlays :areas :overlapping-areas]])] 
      (set-overlapping-areas id)
      (.log js/console (str @overlapping-positions)))))

(defn handle-drag-end [value-path]
  (fn [event] 
    (let [{:keys [active over]} (utils/to-clj-map event)
          id      (:id active)]      
      (dispatch [:db/set [:overlays :areas :dragged] nil]))))      

     

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

;;
;; GRID LAYER
;;

(defn grid-item-drop-zone [{:keys [id component]}]
  (let [ref (r/atom nil)]
    (fn [{:keys [id component]}]
        [:div {:ref (fn [e] (dispatch [:db/set [:overlays :areas :area-dropzones id] e]))
               :style {:background "rgba(0,0,0,0.3)"
                       :display :flex
                       :justify-content :center
                       :align-items :center
                       :color "#DDD"
                       :height "100%"
                       :width "100%"
                       :position :relative}}
           component])))


(defn grid-item [index item]
  [:div {:style {:background "yellow"
                 :color "#222"
                 :height "30px"
                 :width "30px"
                 :display :flex
                 :justify-content :center
                 :align-items :center
                 :border-radius "15px"}}
     (str item)])

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
    (map-indexed (fn [index item] [grid-item-drop-zone {:id index :component [grid-item index item]}])
                 all-area-cells)
    (vector
     (last value-path)
     @(subscribe [:db/get value-path]))]]])

;;
;; AREA LAYER
;;

(def letter-style {:background "#333"
                   :height "30px"
                   :color :white
                   :width "30px"
                   :display :flex
                   :justify-content :center
                   :align-items :center
                   :border-radius "15px"})

(defn area-item-letter [item]
  (let [letter  (fn [] (utils/number-to-letter (:position item)))
        active  (fn [] @(subscribe [:db/get [:overlay :active]]))
        active? (fn [] (= (letter) (active)))]
    [:div {:style letter-style}
       (str (letter))
       (if (active?)
        [:<>
         [expand-horizontal-indicator]
         [expand-vertical-indicator]])]))

(defn area-item-inner [component id]
  (let [position        (:position component)
        area-id     (str "area-" id)]   
     [:div {:id  area-id
            :style {:width "100%" :height "100%"}}            
         [area-item-letter component]]))


(defn draggable-area-style [dragged-letter transform position]
  (let [letter (utils/number-to-letter position)] 
   {:display  (cond
                (= dragged-letter letter) :inherit
                (= dragged-letter nil   ) :inherit
                :else :none)
    :position :relative
    :background (rand-nth utils/random-colors)
    :height "100%"
    :width  "100%"
    :transform (.toString (.-Transform CSS) (clj->js transform))                         
    :grid-area letter}))

(defn area-item [props]
  (let [id                    (:id props)
        component             (:component props)
        dragged-id            (subscribe [:db/get [:overlays :areas :dragged]])
        position              (:position component)
        use-draggable         (utils/to-clj-map (useDraggable (clj->js {:id id})))
        {:keys [attributes
                listeners
                transform
                setNodeRef]}  use-draggable]
    [:div (merge {:style (draggable-area-style @dragged-id transform position) 
                   :class ["area"]
                   :ref (js->clj setNodeRef)}
                 attributes
                 listeners)
      [area-item-inner component id]]))


(defn area-layer [value-path components grid-data]
 (let [the-grid (fn [] @(subscribe [:db/get value-path]))]
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
      (map-indexed (fn [index [item-key item-value]] [:f> area-item {:id        (utils/number-to-letter (:position item-value))
                                                                     :component item-value}])
                   components)
      (vector
       (last value-path)
       (the-grid))]]]))

;;
;; SUMMARY
;;

(defn view []
  (let [overlay (subscribe [:db/get [:overlays :type]])
        value-path            (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
        components-value-path (fn [] (vec (conj (value-path) :components)))
        grid-data             (fn [] @(subscribe [:db/get (value-path)]))
        components            (fn [] @(subscribe [:db/get (components-value-path)]))
        col-count    (fn [] (count (:cols (grid-data))))
        row-count    (fn [] (count (:rows (grid-data))))
        all-area-cells    (fn [] (flatten (:areas (grid-data))))
        items-count  (fn [] (count (components)))
        abc-matrix   (fn [] (utils/generate-abc-matrix (items-count)))]
     (if (= :area @overlay)
      [dnd-context {:onDragStart    (handle-drag-start (value-path))
                    :onDragMove     (handle-drag-move  (value-path))
                    :onDragEnd      (handle-drag-end   (value-path))}
                    ;:modifiers      [restrictToWindowEdges]}[:<>                 
        [grid-layer (value-path) (all-area-cells)]
        [area-layer (value-path) (components) (grid-data)]])))
        