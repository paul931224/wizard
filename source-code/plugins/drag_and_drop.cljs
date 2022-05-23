(ns plugins.drag-and-drop
 (:require [reagent.core :as r]
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


(defn to-clj-map [hash-map]
  (js->clj hash-map :keywordize-keys true))


(defn sortable-item [props]
  (let [{:keys [attributes
                listeners
                setNodeRef
                transform
                transition]} (to-clj-map (useSortable (clj->js {:id (:id props)})))]
    [:div (merge {:id    (str (:id props))
                  :ref   (js->clj setNodeRef)
                  :style {:transform (.toString (.-Transform CSS) (clj->js transform))
                          :transition transition
                          :background "lightgreen"
                          :margin "10px"
                          :border "1px solid yellow"
                          :padding "5px"
                          :border-radius "10px"}}
                 attributes
                 listeners)
     (str (:id props))]))

(defn drag-overlay-item [props]
  [:div (merge {:id    (str (:id props))
                :style {:background "lightgreen"
                        :margin "10px"
                        :border "1px solid yellow"
                        :padding "5px"
                        :border-radius "10px"}})
   (str (:id props))])

(def dnd-context (r/adapt-react-class DndContext))
(def sortable-context (r/adapt-react-class SortableContext))
(def drag-overlay   (r/adapt-react-class DragOverlay))


(defn sortable-example []
  (let [[activeId, setActiveId]  (react/useState nil)
        [items, setItems] (react/useState (clj->js (mapv str (range 100))))
        sensors (useSensors
                 (useSensor PointerSensor)
                 (useSensor KeyboardSensor, (to-clj-map {:coordinateGetter sortableKeyboardCoordinates})))
        handleDragEnd (fn [event]
                        (let [{:keys [active over]} (to-clj-map event)]
                          (let [oldIndex (.indexOf items (:id active))
                                newIndex (.indexOf items (:id over))]
                            (setItems (clj->js (arrayMove (clj->js items) oldIndex newIndex)))))
                        (setActiveId nil))]
    [:div {:style {:width "200px"}}
     [dnd-context {:sensors  sensors
                   :collisionDetection closestCenter
                   :onDragEnd     handleDragEnd
                   :onDragStart  (fn [e] (setActiveId (get (js->clj (aget e "active")) "id")))}
      [sortable-context {:items    items
                         :strategy rectSortingStrategy}
       (map (fn [id] [:f> sortable-item {:id id :key id}])
            items)]
      [drag-overlay [:f> drag-overlay-item {:id (if activeId activeId nil)}]]]]))


(defn view []
  [:div {:style {:margin "100px auto"}}
   [:h1 "Sortable example"]
   [:f> sortable-example]])