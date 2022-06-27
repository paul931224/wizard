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


(defn draggable-body [component id]
  (let [style {:max-height "90vh" :overflow :scroll}]
    [:div {:on-click #(dispatch [:db/set [:editor :toolbar :active] id])
           :style style}
     component]))

(defn draggable-header [listeners label]
  (let [style {:style {:font-weight :bold
                       :padding "10px"
                       :border-bottom "1px solid white"
                       :margin-bottom "10px"}}]
    [:div.toolbar-header (merge listeners style) label]))


(defn draggable [props]
  (let [id                    (:id props)
        label                 (:label props)
        component             (:component props)
        dragged-id            (subscribe [:db/get [:editor :toolbar  :dragged]])
        active-id             (subscribe [:db/get [:editor :toolbar  :active]])
        use-draggable         (utils/to-clj-map (useDraggable (clj->js {:id id})))
        {:keys [attributes
                listeners
                setNodeRef
                transform]}   use-draggable
        moved-style           (subscribe [:db/get [:editor :toolbars id]])
        style                 (fn [] @moved-style)
        dragged?               (fn [] (= @dragged-id id))
        active?               (fn [] (= @active-id id))]
    [:div (merge {:class ["toolbar"
                          (if (dragged?) "dragged" nil)
                          (if (active?)  "active" nil)]
                  :ref (js->clj setNodeRef)
                  :style (style)}
                 attributes)
     [draggable-header listeners label]
     [draggable-body   component id]]))



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