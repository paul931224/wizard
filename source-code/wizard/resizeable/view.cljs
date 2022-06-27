(ns wizard.resizeable.view
 (:require 
   [reagent.core :as r]
   [re-frame.core :refer [dispatch subscribe]]
   ["react" :as react]
   ["@dnd-kit/core" :refer [useDraggable useDroppable DndContext]]
   ["@dnd-kit/utilities" :refer [CSS]]
   ["@dnd-kit/modifiers" :refer [restrictToWindowEdges]]
   [wizard.utils :as utils]))


(def dnd-context (r/adapt-react-class DndContext))

(defn resizeable-item [listeners label component id]
  (let [style {:style {:font-weight :bold
                       :padding "10px"
                       :cursor :resize                       
                       :margin-bottom "10px"}}]
    [:div.resizeable-area 
     (merge listeners style
            {:on-click #(dispatch [:db/set [:editor :toolbar :active] id])})
     component]))



(defn resize-s []
  [:div {:style {:position :absolute
                 :width "100%"
                 :background :red
                 :height "5px"
                 :cursor "s-resize"
                 :bottom 0
                 :left 0}}])

(defn resize-n []
  [:div {:style {:position :absolute
                 :width "100%"
                 :background :red
                 :height "5px"
                 :cursor "n-resize"
                 :top 0
                 :left 0}}])

(defn resize-e []
  [:div {:style {:position :absolute
                 :width "5px"
                 :background :red
                  :height "100%"
                 :cursor "e-resize"
                 :bottom 0
                 :right 0}}])                                  

(defn resize-w []
 [:div {:style {:position :absolute
                :width "5px"
                :background :red 
                :height "100%"
                :cursor "w-resize"
                :bottom 0}}])

(defn draggable [props]
  (let [id                    (:id props)
        label                 (:label props)
        component             (:component props)
        use-draggable         (utils/to-clj-map (useDraggable (clj->js {:id id})))
        {:keys [attributes
                listeners
                setNodeRef]}  use-draggable]
       
       
    [:div (merge {:style {:position :relative}
                  :class ["area"]                          
                  :ref (js->clj setNodeRef)}                 
                 attributes)
     [resizeable-item listeners label component id]
     [resize-w]
     [resize-e]
     [resize-n]
     [resize-s]]))
     
     



(defn handle-drag-start [event]
  (let [{:keys [active over]} (utils/to-clj-map event)
        id      (:id active)]
    (dispatch [:db/set [:editor :toolbar  :dragged] id])))


(defn handle-drag-end [event]
  (let [{:keys [active over]} (utils/to-clj-map event)
        id      (:id active)]
    (dispatch [:db/set [:editor :toolbar  :dragged] nil])
    (dispatch [:db/set [:editor :toolbar  :active] id])))

(defn handle-drag-move [event]
  (let [{:keys [active over]} (utils/to-clj-map event)
        id      (:id active)
        new-pos (-> active :rect :current :translated)
        top-and-left (select-keys new-pos [:top :left])]
    (dispatch [:db/set [:editor :toolbars id] top-and-left])))

(defn toolbar [config]
  [:f> draggable config])

(defn toolbars [content]
  [:div#toolbars {:style {:position :fixed
                          :top 0
                          :z-index 100}}

    [dnd-context {:onDragStart   handle-drag-start
                  :onDragMove     handle-drag-move
                  :onDragEnd      handle-drag-end
                  :modifiers      [restrictToWindowEdges]}
     content]])

(defn area []
 [:div
  "Look at me MeeSeeks"])

(defn view []
 (let [] 
   [:div {:style {:position :relative}}     
     [toolbars 
      [:<> 
       [toolbar {:id "a"
                 :component [area]
                 :label     "Area a"}]]]
     [:div {:style {:height "100px"}}]
     [:div {:style {:height "100px"}}]
     [:div {:style {:height "100px"}}]
     [:div {:style {:height "100px"
                    :width  "400px"
                    :background :beige}} 
      "Dropzone"]]))