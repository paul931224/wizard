(ns plugins.drag-and-drop
 (:require [reagent.core :as r]
           [re-frame.core :refer [dispatch subscribe]]
           ["react" :as react]
           ["@dnd-kit/core" :refer [DndContext
                                    closestCenter
                                    KeyboardSensor
                                    PointerSensor
                                    TouchSensor
                                    DragOverlay
                                    useSensor
                                    useSensors]]
           ["@dnd-kit/sortable" :refer [arrayMove
                                        SortableContext
                                        sortableKeyboardCoordinates
                                        verticalListSortingStrategy
                                        rectSortingStrategy
                                        useSortable]]
           ["@dnd-kit/utilities" :refer [CSS]]))

(def dnd-context (r/adapt-react-class DndContext))
(def sortable-context (r/adapt-react-class SortableContext))
(def drag-overlay   (r/adapt-react-class DragOverlay))

(defn to-clj-map [hash-map]
  (js->clj hash-map :keywordize-keys true))

(defn id-map->ordered-vector [coll]
  (let [pos-to-index (fn [[item-id item-value]]
                        [(:position item-value)
                         (assoc item-value :id item-id)])]
     (mapv second
          (sort-by first
                   (map pos-to-index coll)))))

(defn ordered-vector->id-map [coll]
  (let [index-to-pos (fn [index item]
                       {(:id item)   (-> item 
                                         (dissoc :id)
                                         (assoc  :position index))})]
    (reduce merge (map-indexed index-to-pos coll))))

(defn sortable-style [transform transition]
 {:transform (.toString (.-Transform CSS) (clj->js transform))
  :transition transition
  :background "#333"
  :color "#DDD"
  :margin "10px"
  :border "1.5px solid yellow"
  :padding "5px"
  :border-radius "10px"})

(defn sortable-item [props]
  (let [{:keys [attributes listeners setNodeRef transform transition]} 
        (to-clj-map (useSortable (clj->js {:id (:id props)})))]
    [:div (merge {:id    (str (:id props))
                  :ref   (js->clj setNodeRef)
                  :style (sortable-style transform transition)}
                 attributes
                 listeners)
     [(:component props) (:item props)]]))

(defn get-item-with-id [items id]
 (first (filter 
          (fn [item] (= (:id item) id))
          items)))

(defn sortable-example [prop-items value-path component]
  (let [[activeId, setActiveId]  (react/useState nil)
        [items, setItems] (react/useState (clj->js prop-items))
        sensors (useSensors
                 (useSensor PointerSensor)
                 (useSensor KeyboardSensor, (to-clj-map {:coordinateGetter sortableKeyboardCoordinates})))
        handleDragEnd (fn [event]
                        (let [{:keys [active over]} (to-clj-map event)]
                          (let [items        (to-clj-map items)
                                active-index (:id active)
                                over-index   (:id over)
                                active-item  (get-item-with-id items active-index)
                                over-item    (get-item-with-id items over-index)
                                oldIndex     (.indexOf items active-item)
                                newIndex     (.indexOf items over-item)
                                new-order    (to-clj-map (arrayMove (clj->js items) oldIndex newIndex))
                                new-order-js (clj->js new-order)]                            
                            (dispatch [:db/set value-path (ordered-vector->id-map new-order)])
                            (setItems new-order-js)))
                        (setActiveId nil))]
    [dnd-context {:sensors  sensors
                  :collisionDetection closestCenter
                  :onDragEnd     handleDragEnd
                  :onDragStart  (fn [e] 
                                 (setActiveId (get (js->clj (aget e "active")) "id")))}
          [sortable-context {:items    items
                             :strategy verticalListSortingStrategy}
           (map (fn [item] (let [clj-item (to-clj-map item)] 
                            [:f> sortable-item {:id   (:id clj-item)
                                                :key  (:id clj-item)
                                                :item clj-item 
                                                :component component}])) 
                                                
                items)]]))
       ;[drag-overlay [:f> drag-overlay-item {:id (if activeId activeId nil)}]]]))






(def when-in-may {"id3" {:type :grid
                         :position 1}
                  "id2" {:type :grid
                         :position 0}
                  "id4" {:type :grid 
                         :position 2}
                  "id5" {:type :grid
                         :position 3}}) 

(ordered-vector->id-map 
 (id-map->ordered-vector 
   when-in-may))
               

(defn view [{:keys [value-path component]}]
  (let [items (subscribe [:db/get value-path])] 
    [:div (str @items)
     (if @items 
      ^{:key (str @items)}[:f> sortable-example (id-map->ordered-vector @items) value-path component])]))