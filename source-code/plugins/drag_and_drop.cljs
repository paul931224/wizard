(ns plugins.drag-and-drop
 (:require [reagent.core :as r]
           [re-frame.core :refer [dispatch subscribe]]
           ["react" :as react]
           [wizard.utils :as utils]
           [reagent-hickory.sweet :refer [html->hiccup]]
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

(defn sortable-handle-style []
 {
  :flex-grow 1})

(defn sortable-container-style [transform transition]
 {:transform (.toString (.-Transform CSS) (clj->js transform))
  :transition transition
  :display :flex
  :margin-bottom "5px"
  :border "1px solid #222"
  :border-radius "5px"
  :background "#666"
  :color "#DDD"
  :padding "5px"
  :cursor :pointer}) 
  ;:width "100%"})
  
  

(defn sortable-item [props]
  (let [item            (:item props)
        id              (:id item)
        type            (:type item)
        components      (:components item)
        component       (:component props)
        component-data  (:component-data props)
        path            (:path props)
        new-path        (vec (concat path [id])) 
        {:keys [attributes listeners setNodeRef transform transition]} 
        (utils/to-clj-map (useSortable (clj->js {:id (str id)})))]
    [:div {:style (sortable-container-style transform transition)}
          [:div 
            (merge {:style (sortable-handle-style)
                    :on-mouse-enter #(dispatch [:db/set [:editor :hovered-component] id])
                    :on-mouse-leave #(dispatch [:db/set [:editor :hovered-component] nil])
                    :id    (str id)
                    :ref   (js->clj setNodeRef)}
                    
              attributes)
              
                         
            [:div (merge {:style {:font-weight :bold
                                  :padding-bottom "10px"}}
                         listeners)
                  (str type)] ;" "(html->hiccup  (:content (:item props)))]            
            (if components
                 [component component-data new-path])]
          [:div {:style {:border "1px solid white"
                         :padding "5px"}
                 :on-click (fn [e] 
                            (.preventDefault e)
                            (.stopPropagation e)
                            (dispatch [:db/set [:editor :selected :value-path] new-path]))}
              "S"]]))       
          

(defn get-item-with-id [items id]
 (first (filter 
          (fn [item] (= (:id item) id))
          items)))

(defn sortable-example [prop-items value-path component component-data]
  (let [[activeId, setActiveId]  (react/useState nil)
        [items, setItems] (react/useState (clj->js prop-items))
        sensors (useSensors
                 (useSensor PointerSensor)
                 (useSensor KeyboardSensor, (utils/to-clj-map {:coordinateGetter sortableKeyboardCoordinates})))
        handleDragEnd (fn [event]
                        (let [{:keys [active over]} (utils/to-clj-map event)]
                          (let [items        (utils/to-clj-map items)
                                active-index (:id active)
                                over-index   (:id over)
                                active-item  (get-item-with-id items active-index)
                                over-item    (get-item-with-id items over-index)
                                oldIndex     (.indexOf items active-item)
                                newIndex     (.indexOf items over-item)
                                new-order    (utils/to-clj-map (arrayMove (clj->js items) oldIndex newIndex))
                                new-order-js (clj->js new-order)]                            
                            (dispatch [:db/set value-path (utils/ordered-vector->id-map new-order)])
                            (setItems new-order-js)))
                        (setActiveId nil))]
    [dnd-context {:sensors  sensors
                  :collisionDetection closestCenter
                  :onDragEnd     handleDragEnd
                  :onDragStart  (fn [e] 
                                 (setActiveId (get (js->clj (aget e "active")) "id")))}
          [sortable-context {:items    items
                             :strategy verticalListSortingStrategy}
           (map (fn [item] (let [clj-item (utils/to-clj-map item)] 
                            [:f> sortable-item {:id   (:id clj-item)
                                                :key  (:id clj-item)
                                                :item clj-item 
                                                :component component 
                                                :component-data component-data
                                                :path value-path}])) 
                                                
                items)]]))
       ;[drag-overlay [:f> drag-overlay-item {:id (if activeId activeId nil)}]]]))


(defn view [{:keys [value-path component component-data]}]
  (let [items (subscribe [:db/get value-path])] 
    (if @items 
      ^{:key (str @items)}
      [:f> sortable-example 
       (utils/id-map->ordered-vector @items)
       value-path
       component
       component-data])))