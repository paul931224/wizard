(ns wizard.overlays.area
  (:require  [reagent.core :as r]
             [re-frame.core :refer [dispatch subscribe]]
             [wizard.overlays.wrapper :as overlay-wrapper]
             ["react" :as react]
             ["@dnd-kit/core" :refer [closestCenter
                                      KeyboardSensor
                                      PointerSensor
                                      TouchSensor
                                      DragOverlay
                                      useSensor
                                      useSensors
                                      useDraggable 
                                      useDroppable 
                                      DndContext]]
             ["@dnd-kit/utilities" :refer [CSS]]
             ["@dnd-kit/modifiers" :refer [restrictToWindowEdges restrictToHorizontalAxis restrictToFirstScrollableAncestor]]
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

(defn keep-only-letter-coordinates [letters-with-coordinates]
 (filter (fn [[letter row-index col-index]]
          (not= letter "."))
         letters-with-coordinates))  


(defn everything-identical? [coll]
 (let [set-count (count (set coll))] 
  (boolean 
    (or (= 1 set-count)
        (= 0 set-count)))))

(defn same-amount-of-rows? [cells]
 (let [point? (= 1 (count cells))
       row-index-counts (mapv
                         (fn [[row-index values]] (count values))
                         (group-by second cells))]    
  (if point?
         ;This must be a rect if it's one point
      true
      (everything-identical? row-index-counts))))
      
 

(defn correct-area-config? [config selected-area]
 (let [letter-positions (vec (reduce concat (map-indexed 
                                                (fn [row-index col-values]
                                                    (map-indexed 
                                                     (fn [col-index value]
                                                      [value row-index col-index])
                                                     col-values))
                                             config)))
       only-letter-positions (keep-only-letter-coordinates letter-positions)
       without-selected-area (vec (filter 
                                   (fn [cell] (not= selected-area (first cell)))
                                   only-letter-positions))
                                               
       grouped-by-letters-and-counted (mapv (fn [[letter cells]] (same-amount-of-rows? cells)) 
                                            (group-by first without-selected-area))]
   (everything-identical? grouped-by-letters-and-counted))) 
 

(defn modify-areas [{:keys [area-to-fill areas-config indexes-overlapped]}]
    (let [column-count     (count (first areas-config))
          flattened-config (vec (flatten areas-config))
          overlapping?     (fn [index] (some 
                                        (fn [overlapped-index] (= overlapped-index index)) 
                                        indexes-overlapped))
          decide-character (fn [index letter] (cond 
                                               (overlapping? index)          area-to-fill
                                               (= letter area-to-fill)       "."
                                               :else                         letter))
          new-characters   (vec (map-indexed  decide-character flattened-config))
          new-config       (mapv vec (partition column-count new-characters))]                                                
      new-config))
     
     


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


(defn calculate-overlapping-areas [id]
  (let [area-dropzones    (subscribe [:db/get [:overlays :areas :area-dropzones]])
        overlapping-areas (get-overlapping-areas
                           (dom-utils/get-rect-data (.getElementById js/document (str "area-" id)))
                           (mapv
                            (fn [a] [(first a) (dom-utils/get-rect-data (second a))])
                            @area-dropzones))]
    overlapping-areas))


(defn handle-drag-start [value-path]
  (fn [event] 
   (let [{:keys [active over]} (utils/to-clj-map event)
         area      (:id active)
         overlapping-areas      (calculate-overlapping-areas area)]
     (.log js/console "Drag started.")
     (dispatch [:db/set [:overlays :areas :overlapping-areas] overlapping-areas])
     (dispatch [:db/set [:overlays :areas :active]  area])
     (dispatch [:db/set [:overlays :areas :dragged] area]))))

(defn handle-drag-move [value-path]
  (fn [event]
    (let [{:keys [active over]}  (utils/to-clj-map event)
          area                   (:id active)
          overlapping-areas      (calculate-overlapping-areas area)
          areas-path             (vec (concat value-path [:areas]))
          areas                  @(subscribe [:db/get areas-path])
          modified-areas         (modify-areas {:area-to-fill        areas
                                                :areas-config        areas
                                                :indexes-overlapped  overlapping-areas})
          possible-config?       (correct-area-config? modified-areas area)]                            
      (dispatch [:db/set [:overlays :areas :possible-config?]  possible-config?])
      (dispatch [:db/set [:overlays :areas :overlapping-areas] overlapping-areas]))))
      

(defn handle-drag-end [value-path]
  (fn [event] 
    (let [{:keys [active over]} (utils/to-clj-map event)
          area                   (:id active)
          areas-path             (vec (concat value-path [:areas]))
          grid-areas             @(subscribe [:db/get areas-path])
          overlapping-positions  @(subscribe [:db/get [:overlays :areas :overlapping-areas]])
          possible-config?       @(subscribe [:db/get [:overlays :areas :possible-config?]])
          modified-areas         (modify-areas {:area-to-fill        area 
                                                :areas-config        grid-areas
                                                :indexes-overlapped  overlapping-positions})    
          area-config-correct? (correct-area-config? modified-areas area)]            
      (if possible-config?
        (dispatch [:db/set areas-path modified-areas]))                            
      (dispatch [:db/set [:overlays :areas :dragged] nil])
      (dispatch [:db/set [:overlays :areas :possible-config?]  true])
      (dispatch [:db/set [:overlays :areas :overlapping-areas] []]))))

(defn handle-resize-start [resize-direction]
  (fn [event]
     (let [{:keys [active over]}  (utils/to-clj-map event)
             area                   (:id active)
             original-area-rect     (dom-utils/get-rect-data (.getElementById js/document (str "area-" area)))]
      (.log js/console "Resize started.: " original-area-rect)
      (dispatch [:db/set [:overlays :areas :dragged] nil])
      (dispatch [:db/set [:overlays :areas :resized] area])
      (dispatch [:db/set [:overlays :areas :resized-area-rect] original-area-rect])
      (dispatch [:db/set [:overlays :areas :resize-direction] resize-direction])
      (dispatch [:db/set [:overlays :areas :resize-delta] {:x 0 :y 0}]))))

(defn handle-resize-move [resize-direction]
  (fn [event]
    (let [{:keys [active over]} (utils/to-clj-map event)
          area         (:id active)
          event-delta  (utils/to-clj-map (.-delta event))
          x-delta      (:x event-delta)
          y-delta      (:y event-delta)
          overlapping-areas      (calculate-overlapping-areas area)]
      (dispatch [:db/set [:overlays :areas :resized] area])
      (dispatch [:db/set [:overlays :areas :resize-direction] resize-direction])
      (dispatch [:db/set [:overlays :areas :resize-delta] {:x x-delta
                                                           :y y-delta}])
      (.log js/console "Resizing." overlapping-areas))))

(defn handle-resize-end []
  (fn [event]
    (let [{:keys [active over]} (utils/to-clj-map event)
          area      (:id active)
          overlapping-areas      (calculate-overlapping-areas area)]
      (.log js/console "Resize ended.")
      (dispatch [:db/set [:overlays :areas :resized] nil])
      (dispatch [:db/set [:overlays :areas :resize-direction] nil])
      (dispatch [:db/set [:overlays :areas :resized-area-rect] nil]))))     

(def north-style
  {:position :absolute
   :height "5px"
   :width  "100%"
   :background "blue"
   :top 0
   :cursor "n-resize"})

(def east-style
  {:position :absolute
   :height "100%"
   :background "red"
   :width  "5px" 
   :right 0
   :cursor "e-resize"})

(def south-style
  {:position :absolute
   :height "5px"
   :width  "100%"
   :background "blue"
   :bottom 0
   :cursor "s-resize"})

(def west-style
  {:position :absolute
   :height "100%"
   :background "red"
   :width  "5px"
   :left 0
   :cursor "w-resize"})


(defn resize-indicator-draggable [id style]
  (let [use-draggable         (utils/to-clj-map (useDraggable (clj->js {:id id})))
        {:keys [attributes listeners setNodeRef]} use-draggable]
     [:div (merge attributes listeners
                 {:ref (js->clj setNodeRef)
                  :style style})]))

(defn resize-indicator-context [id direction style]
  (let [sensors (useSensors
                 (useSensor PointerSensor)
                 (useSensor KeyboardSensor, TouchSensor))]
    [dnd-context {:sensors  sensors
                  :collisionDetection closestCenter
                  :onDragStart    (handle-resize-start direction)
                  :onDragMove     (handle-resize-move  direction)
                  :onDragEnd      (handle-resize-end)}
     [:f> resize-indicator-draggable id style]]))                                

(defn resize-indicators [id]
 [:<>
  [:f> resize-indicator-context id :north north-style]
  [:f> resize-indicator-context id :east  east-style]
  [:f> resize-indicator-context id :south south-style]
  [:f> resize-indicator-context id :west  west-style]])

;;
;; GRID LAYER
;;

(defn grid-item-drop-zone [{:keys [id component]}]
  (let [overlapping-areas   (subscribe [:db/get [:overlays :areas :overlapping-areas]])
        impossible-config?  (fn [] (not @(subscribe [:db/get [:overlays :areas :possible-config?]])))
        overlapping?        (fn [] 
                             (boolean (some (fn [overlapped-index]
                                             (= id overlapped-index))
                                            @overlapping-areas)))]        
    (fn [{:keys [id component]}]
        [:div {:ref (fn [e] (dispatch [:db/set [:overlays :areas :area-dropzones id] e]))
               :style {:background (cond 
                                    (impossible-config?)  "rgba(255,0,0,0.8)"
                                    (overlapping?)        "rgba(0,255,0,0.3)"                                    
                                    :else                 "rgba(0,0,0,0.3)")
                                    
                       :display :flex
                       :justify-content :center
                       :align-items :center
                       :color "#DDD"
                       :height "100%"
                       :width "100%"}}                       
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
    (map-indexed (fn [index item] [grid-item-drop-zone {:id index
     
                                                        :component [grid-item index item]}])
                 all-area-cells)
    (last value-path)
    @(subscribe [:db/get value-path])]]])

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
                   :position :relative
                   :border-radius "15px"})

(defn area-item-letter [letter]
  [:div {:style letter-style}
       (str (letter))])
       

(defn area-item-inner [component id]
  (let [position    (:position component)
        area-id     (str "area-" id)
        letter      (fn [] (utils/number-to-letter position))]   
     [:div {:id  area-id
            :style {:width "100%" :height "100%"
                    :position :relative
                    :background (rand-nth utils/random-colors)}}            
         [area-item-letter letter]]))
         

(defn resize-transform [{:keys [direction area-width area-height delta-y delta-x]}]
 (let [new-area-width  (+ area-width  delta-x)
       new-area-height (+ area-height delta-y)
       scale-x         (/ area-width  new-area-width)
       scale-y         (/ area-height new-area-height)
       half-delta-x   (/ delta-x 2)
       half-delta-y   (/ delta-y 2)
       north?         (= direction :north)
       east?          (= direction :east)
       south?         (= direction :south)
       west?          (= direction :west)
       horizontal?    (or east? west?)
       vertical?      (or north? south?)
       scale-str      (str 
                       "scale(" 
                       (if horizontal? scale-x "1") 
                       "," 
                       (if vertical?   scale-y "1")
                       ")")
       translate-str   (str 
                        "translate(" 
                        (if horizontal? half-delta-x "0")
                        "px, " 
                        (if vertical?   half-delta-y "0")
                        "px)")]         
    
    (str scale-str " " translate-str)))
         


(defn draggable-area-style [dragged-letter resized-letter transform position]
  (let [letter            (utils/number-to-letter position)
        resize-delta      (subscribe [:db/get [:overlays :areas :resize-delta]])
        resize-direction  (subscribe [:db/get [:overlays :areas :resize-direction]])
        delta-x           (:x @resize-delta)
        delta-y           (:y @resize-delta)
        area-rect         (subscribe [:db/get [:overlays :areas :resized-area-rect]])
        area-width        (:width  @area-rect)
        area-height       (:height @area-rect)]
         
   {:display  (cond
                (= dragged-letter letter)      :inherit                
                (= resized-letter letter)      :inherit
                (and (= dragged-letter nil)
                     (= resized-letter nil))   :inherit
                :else                          :none)
    :position :relative    
    :height "100%"
    :width  "100%"
    :transform (cond 
                dragged-letter (.toString (.-Transform CSS) (clj->js transform))
                resized-letter (resize-transform {:direction @resize-direction
                                                  :area-height area-height 
                                                  :area-width  area-width 
                                                  :delta-x     delta-x 
                                                  :delta-y     delta-y})
                :else     "")                         
    :grid-area letter}))

(defn area-item [props]
  (let [id                    (:id props)
        component             (:component props)
        position              (:position component)
        dragged-letter (subscribe [:db/get [:overlays :areas :dragged]])
        resized-letter (subscribe [:db/get [:overlays :areas :resized]])
        use-draggable         (utils/to-clj-map (useDraggable (clj->js {:id id})))
        letter                (fn [] (utils/number-to-letter (:position component)))
        {:keys [attributes
                listeners
                transform
                setNodeRef]}  use-draggable
        active      (fn [] @(subscribe [:db/get [:overlays :areas :active]]))
        active?     (fn [] (= (letter) (active)))]        
    [:div (merge {:style (merge 
                          (draggable-area-style @dragged-letter @resized-letter transform position))      
                  :class ["area"]})
                   
      [:div (merge 
             {:ref (js->clj setNodeRef)}
 
             attributes 
             listeners) 
        [area-item-inner component id]]    
      [resize-indicators id]]))
     


(defn area-layer [value-path components grid-data]
 (let [the-grid (fn [] @(subscribe [:db/get value-path]))
       areas    (flatten (:areas (the-grid)))
       available-area? (fn [area] (boolean 
                                   (some (fn [this-area]
                                          (= this-area area))
                                    areas)))]
                      
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
      (map-indexed (fn [index [item-key item-value]]                     
                    (let [letter (utils/number-to-letter (:position item-value))] 
                      (if (available-area? letter) 
                       [:f> area-item {:id        letter
                                       :component item-value}])))
                   components)
      (last value-path)
      (the-grid)]]
    {:overflow-x :hidden 
     :overflow-y :hidden}]))

;;
;; SUMMARY
;;

(defn functional-view []
  (let [overlay (subscribe [:db/get [:overlays :type]])
        value-path            (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
        components-value-path (fn [] (vec (conj (value-path) :components)))
        grid-data             (fn [] @(subscribe [:db/get (value-path)]))
        components            (fn [] @(subscribe [:db/get (components-value-path)]))
        col-count    (fn [] (count (:cols (grid-data))))
        row-count    (fn [] (count (:rows (grid-data))))
        all-area-cells    (fn [] (flatten (:areas (grid-data))))
        items-count  (fn [] (count (components)))
        abc-matrix   (fn [] (utils/generate-abc-matrix (items-count)))
        sensors (useSensors
                 (useSensor PointerSensor)
                 (useSensor KeyboardSensor, TouchSensor))]
        
     (if (= :area @overlay)
      [dnd-context {:onDragStart    (handle-drag-start (value-path))
                    :onDragMove     (handle-drag-move  (value-path))
                    :onDragEnd      (handle-drag-end   (value-path))
                    :sensors            sensors
                    :collisionDetection closestCenter}
        [:<>                 
         [grid-layer (value-path) (all-area-cells)]
         [area-layer (value-path) (components) (grid-data)]]])))
        

(defn view []
 [:f> functional-view])        