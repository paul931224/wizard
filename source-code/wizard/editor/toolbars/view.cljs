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

(def draggable-window-style {:position :fixed
                             :background "#333"
                             :padding "5px 10px"
                             :min-width "200px"})

(defn draggable [props]
 (let [id (:id props)
       label (:label props)
       {:keys [attributes listeners setNodeRef  transform]} 
       (to-clj-map (useDraggable (clj->js {:id id})))
       moved-style (subscribe [:db/get [:editor :toolbars id]])]
      [:div (merge
             {:ref (js->clj setNodeRef)
              :style (merge
                      {:cursor :pointer
                       :color "#DDD"
                       :max-height "90vh"
                       :overflow :scroll}
                      draggable-window-style
                      @moved-style)}            
             attributes)
       [:div (merge 
              {:ref (js->clj setNodeRef)}
              listeners)
             label]       
       [:div 
        (:component props)]]))


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
   