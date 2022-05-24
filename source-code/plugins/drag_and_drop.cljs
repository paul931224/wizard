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
     (str props)]))

(defn get-item-with-id [items id]
 (first (filter 
          (fn [item] (= (:id item) id))
          items)))


(defn sortable-example [prop-items value-path]
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
                                new-order    (js->clj (arrayMove (clj->js items) oldIndex newIndex))
                                new-order-js (clj->js new-order)]
                            (js/console.log oldIndex " - "items)
                            (dispatch [:db/set (:value-path prop-items) new-order])
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
                                                :value-path value-path}]))
                items)]]))
       ;[drag-overlay [:f> drag-overlay-item {:id (if activeId activeId nil)}]]]))


(defn view [{:keys [value-path]}]
  (let [items (subscribe [:db/get value-path])] 
   (if @items 
    [:f> sortable-example @items value-path])))