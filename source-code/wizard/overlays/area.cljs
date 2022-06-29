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

(defn get-overlapping-areas [this-area areas]
  (vec
   (filter
    (fn [area]
      (let [this-top    (:top    this-area)
            this-bottom (:bottom this-area)
            this-left   (:left   this-area)
            this-right  (:right  this-area)
            area-top    (:top    (second area))
            area-bottom (:bottom (second area))
            area-left   (:left   (second area))
            area-right  (:right  (second area))]        
         (or (and (>= this-bottom area-top)
                  (<= this-top    area-bottom))
            (and  (>= this-left   area-right)
                  (<= this-right  area-left)))))
    areas)))


(defn resizeable-item [resize-data component id]
  (let [ref (atom nil)
        area-dropzones  (subscribe [:db/get [:area-dropzones]])
        areas-overlapped (atom [])
        style {:style {:font-weight :bold
                       :width "100%"
                       :cursor :resize
                       :height "100%"}}
        set-overlapping-areas (fn [e]
                                (.log js/console (str (mapv first (get-overlapping-areas
                                                                   (dom-utils/get-rect-data @ref)
                                                                   @area-dropzones))))
                                (dispatch [:db/set [:overlapping-areas]
                                           (get-overlapping-areas
                                            (dom-utils/get-rect-data @ref)
                                            @area-dropzones)]))]
    (r/create-class
     {:component-did-mount  set-overlapping-areas
      :component-did-update set-overlapping-areas
      :reagent-render
      (fn [resize-data component id]
       [:div.resizeable-area
        (merge style
               {:ref (fn [e] (reset! ref e))
                :on-click #(dispatch [:db/set [:editor :toolbar :active] id])})
        component])})))


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



(defn resizeable-area-inner [props]
  (let [id                    (:id props)
        component             (:component props)
        use-draggable         (utils/to-clj-map (useDraggable (clj->js {:id id})))
        {:keys [attributes
                listeners
                setNodeRef]}  use-draggable]


    [:div (merge {:style {:position :relative
                          ;:height (str (:height @resize-atom) "px")
                          ;:width  (str (:width  @resize-atom)  "px")
                          :width "100%"
                          :grid-area (utils/number-to-letter (:position (second id)))}
                  :class ["area"]
                  :ref (js->clj setNodeRef)}
                 attributes
                 listeners)
     [resizeable-item @resize-atom component id]]))

(defn resizeable-area [config]
  [:f> resizeable-area-inner config])












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
;; UTILS
;;

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


;;
;; GRID LAYER
;;

(defn grid-item-drop-zone [{:keys [id component]}]
  (let [ref (r/atom nil)]
    (r/create-class
     {:component-did-mount  (fn [e] (dispatch [:db/set [:area-dropzones id] (dom-utils/get-rect-data @ref)]))
      :component-did-update (fn [e] (dispatch [:db/set [:area-dropzones id] (dom-utils/get-rect-data @ref)]))
      :reagent-render
      (fn [props]
        [:div {:ref (fn [e] (reset! ref e))}

         component])})))

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
    (map-indexed (fn [index item] [area-item])
                 components)
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
        abc-matrix   (fn [] (generate-abc-matrix (items-count)))]
     (if (= :area @overlay)
      [dnd-context {:onDragStart   handle-drag-start
                    :onDragMove     handle-drag-move
                    :onDragEnd      handle-drag-end}
                    ;:modifiers      [restrictToWindowEdges]}[:<>                 
        [grid-layer (value-path) (all-area-cells)]
        [area-layer (value-path) (components) (grid-data)]])))
        