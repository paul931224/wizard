(ns wizard.editor.toolbars.view
 (:require 
  [reagent.core :as r]
  [re-frame.core :refer [dispatch subscribe]]
  [wizard.editor.toolbars.rich-text-editor.core :as rte]
  [wizard.editor.toolbars.components.core :as components]
  [wizard.editor.toolbars.order.core :as order]
  ["react" :as react]
  ["@dnd-kit/core" :refer [useDraggable useDroppable DndContext]]
  ["@dnd-kit/utilities" :refer [CSS]]
  ["@dnd-kit/modifiers" :refer [restrictToWindowEdges]]))

(def dnd-context (r/adapt-react-class DndContext))

(defn to-clj-map [hash-map]
  (js->clj hash-map :keywordize-keys true))


(defn draggable-body [component]
 (let [style {:max-height "90vh" :overflow :scroll}] 
  [:div {:style style} component]))

(defn draggable-header [listeners label]
 (let [style {:style {:cursor :pointer}}] 
   [:div (merge listeners style) label])) 
        

(defn draggable [props]
 (let [id                    (:id props)
       label                 (:label props)
       component             (:component props)
       use-draggable         (to-clj-map (useDraggable (clj->js {:id id})))
       {:keys [attributes 
               listeners
               setNodeRef  
               transform]}   use-draggable 
       moved-style           (subscribe [:db/get [:editor :toolbars id]])
       style                 (fn [] (merge {:background     "#333"
                                            :color          "#DDD"
                                            :display        :flex
                                            :flex-direction :column
                                            :min-width      "200px"
                                            :padding        "5px 10px"
                                            :position       :fixed}                              
                                           @moved-style))]
      [:div (merge {:ref (js->clj setNodeRef)
                    :style (style)}            
                   attributes)
       [draggable-header listeners label]       
       [draggable-body   component]]))


(defn components-window []
 [:div [components/view]])

(defn order-window []
 [:div [order/view]])

(defn rte-window []
 (let [value-path (subscribe [:db/get [:rich-text-editor :value-path]])]
      (if @value-path
         [rte/view {:value-path @value-path}])))

(defn view []
 (let [handle-drag-move (fn [event]
                          (let [{:keys [active over]} (to-clj-map event)
                                id      (:id active)
                                new-pos (-> active :rect :current :translated)
                                top-and-left (select-keys new-pos [:top :left])]
                            (dispatch [:db/set [:editor :toolbars id] top-and-left])))]
                                                                                     
  [:div 
   [dnd-context {:onDragMove    handle-drag-move
                 :modifiers     [restrictToWindowEdges]}
                [:f> draggable {:id "order-window"       
                                :component [order-window]
                                :label "Order"}]
                [:f> draggable {:id "rte-window"  
                                :component [rte-window]         
                                :label "Rich Text Editor"}]
                [:f> draggable {:id "components-window"  
                                :component [components-window]    
                                :label "Components"}]]]))
   