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
         (println
           (> first-left  second-right) 
           (> second-left first-right) 
           (> first-top  second-bottom) 
           (> second-top first-bottom) " " first-height)
          
         (cond 
          (> first-left  second-right)  false
          (> second-left first-right)  false
          (> first-top  second-bottom) false 
          (> second-top first-bottom)  false
          :else true)))
     areas))))


(defn handle-drag-start [event]
  (let [{:keys [active over]} (utils/to-clj-map event)
        id      (:id active)]
    (dispatch [:db/set [:editor :toolbar  :dragged] id])))


(defn handle-drag-end [event]
  (let [{:keys [active over]} (utils/to-clj-map event)
        id      (:id active)]
    (reset! resize-atom (dissoc @resize-atom :bottom :top :right :left))
    (dispatch [:db/set [:area-editor :dragged] nil])
    (dispatch [:db/set [:area-editor :active] id])))


(defn handle-drag-move [event]
  (let [{:keys [active over]} (utils/to-clj-map event)
        id      (:id active)
        new-pos (-> active :rect :current :translated)

        old-directions (select-keys @resize-atom [:bottom :top
                                                  :left   :right])
        new-directions (select-keys new-pos [:bottom :top
                                             :left   :right])
        left-delta     (- (:left  old-directions) (:left  new-directions))
        top-delta      (- (:top   old-directions) (:top   new-directions))]

    (if (contains? old-directions :right)
      (reset! resize-atom (merge @resize-atom
                                 {:width  (- (:width @resize-atom) left-delta)
                                  :height (- (:height @resize-atom) top-delta)
                                  :top    (- (:top  @resize-atom)   top-delta)
                                  :left   (- (:left @resize-atom) left-delta)}))
      (reset! resize-atom (merge @resize-atom new-directions)))))



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
        [:div {:ref (fn [e] (dispatch [:db/set [:area-dropzones id] e]))
               :style {:background "red"
                       :display :flex
                       :justify-content :center
                       :align-items :center
                       :color "#DDD"
                       :height "100%"
                       :width "100%"
                       :position :relative}}
           component])))


(defn grid-item [index item]
  [:div {:style {:background "#333"
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

(defn set-overlapping-areas-func [position ref]
  (fn [e] 
    (let [area-dropzones    (subscribe [:db/get [:area-dropzones]])
          overlapping-areas (get-overlapping-areas
                             (dom-utils/get-rect-data @ref)
                             (mapv 
                              (fn [a] [(first a) (dom-utils/get-rect-data (second a))])
                              @area-dropzones))]
      (.log js/console "I will set: "  (dom-utils/get-rect-data @ref))
      (dispatch [:db/set [:overlapping-areas] overlapping-areas]))))

(defn area-item-inner [resize-data component id]
  (let [ref (r/atom nil)
        position        (:position component)]   
    (r/create-class
     {:component-did-mount  (set-overlapping-areas-func position ref)
      :component-did-update (set-overlapping-areas-func position ref)
      :reagent-render
      (fn [resize-data component id]
        [:div {:ref (fn [a] (reset! ref a))
               :style {:width "100%" :height "100%"}
               :on-click #(dispatch [:db/set [:editor :toolbar :active] id])}
         [area-item-letter component]])})))

(defn draggable-area-style [transform position]
  {:position :relative
   :background (rand-nth utils/random-colors)
   :height "100%"
   :width  "100%"
   :transform (.toString (.-Transform CSS) (clj->js transform))                         
   :grid-area (utils/number-to-letter position)})

(defn area-item [props]
  (let [id                    (:id props)
        component             (:component props)
        position              (:position component)
        use-draggable         (utils/to-clj-map (useDraggable (clj->js {:id id})))
        {:keys [attributes
                listeners
                transform
                setNodeRef]}  use-draggable]
    [:div (merge {:style (draggable-area-style transform position) 
                  :class ["area"]
                  :ref (js->clj setNodeRef)}
                 attributes
                 listeners)
     [area-item-inner @resize-atom component id]]))


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
    (map-indexed (fn [index [item-key item-value]] [:f> area-item {:id        item-key
                                                                   :component item-value}])
                 (vector (first components)))
    (vector
     (last value-path)
     @(subscribe [:db/get value-path]))]]])

;;
;; SUMMARY
;;

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
        abc-matrix   (fn [] (utils/generate-abc-matrix (items-count)))]
     (if (= :area @overlay)
      [dnd-context {:onDragStart    handle-drag-start
                    :onDragMove     handle-drag-move
                    :onDragEnd      handle-drag-end}
                    ;:modifiers      [restrictToWindowEdges]}[:<>                 
        [grid-layer (value-path) (all-area-cells)]
        [area-layer (value-path) (components) (grid-data)]])))
        