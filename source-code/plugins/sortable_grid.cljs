(ns plugins.sortable-grid
  (:require [reagent.core :as r]
            [re-frame.core :refer [dispatch subscribe]]
            ["react" :as react]
            [wizard.utils :as utils]
            [reagent-hickory.sweet :refer [html->hiccup]]
            [wizard.editor.grid :as grid]
            ["@dnd-kit/core" :refer [DndContext
                                     closestCenter
                                     KeyboardSensor
                                     PointerSensor
                                     TouchSensor
                                     DragOverlay
                                     useSensor
                                     useSensors]]
            ["@dnd-kit/sortable" :refer [arrayMove
                                         arraySwap
                                         SortableContext
                                         sortableKeyboardCoordinates
                                         verticalListSortingStrategy
                                         rectSortingStrategy
                                         rectSwappingStrategy
                                         useSortable]]
            ["@dnd-kit/utilities" :refer [CSS]]))

(def dnd-context (r/adapt-react-class DndContext))
(def sortable-context (r/adapt-react-class SortableContext))
(def drag-overlay   (r/adapt-react-class DragOverlay))

(defn to-clj-map [hash-map]
  (js->clj hash-map :keywordize-keys true))


(defn sortable-container-style [transform transition]
  {:transform (.toString (.-Transform CSS) (clj->js transform))
   :transition transition
   :display :flex
   :justify-content :center 
   :align-items :center
   :cursor :pointer
   :position :relative
   :width "100%"})
   




(defn sortable-item [props]
  (let [item            (:item props)
        id              (:id item)
        type            (:type item)
        component-data  (:component-data props)
        path            (:path props)
        position        (:position props)
        new-path        (vec (concat path [id]))
        {:keys [attributes listeners setNodeRef transform transition]}
        (to-clj-map (useSortable (clj->js {:id (str id)})))]
    [:div (merge {:id    (str id)
                  :ref   (js->clj setNodeRef)
                  :style (sortable-container-style transform transition)}
                 attributes
                 listeners)
     [:div {:style {:background "rgba(0,0,255,0.3)"
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
       (str (inc position))]]]))
    
       
        


(defn get-item-with-id [items id]
  (first (filter
          (fn [item] (= (:id item) id))
          items)))

(defn sortable-example [prop-items value-path components-value-path]
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
                                new-order    (to-clj-map (arraySwap (clj->js items) oldIndex newIndex))
                                new-order-js (clj->js new-order)]
                            (dispatch [:db/set components-value-path (utils/ordered-vector->id-map new-order)])
                            (setItems new-order-js)))
                        (setActiveId nil))]
    [dnd-context {:sensors  sensors
                  :collisionDetection closestCenter
                  :onDragEnd     handleDragEnd
                  :onDragStart  (fn [e]
                                  (setActiveId (get (js->clj (aget e "active")) "id")))}
        [sortable-context {:items    items
                           :strategy rectSwappingStrategy}
         [grid/grid-wrapper
          (map-indexed (fn [index item] (let [clj-item (to-clj-map item)]
                                         [:f> sortable-item {:position index
                                                             :id   (:id clj-item)
                                                             :key  (:id clj-item)
                                                             :item clj-item
                                                             :path value-path}]))
               items)
          (vector 
                 (last value-path) 
                 @(subscribe [:db/get value-path]))]]]))
    ;[drag-overlay [:f> drag-overlay-item {:id (if activeId activeId nil)}]]]))


(defn view [{:keys [value-path]}]
  (let [components-value-path (vec (conj value-path :components))
        this   (subscribe [:db/get value-path])
        items  (subscribe [:db/get components-value-path])]
    (if (and @items (= (:type @this) "grid"))
      ^{:key (str @items)}
      [:f> sortable-example
        (utils/id-map->ordered-vector @items)
        value-path
        components-value-path])))
       