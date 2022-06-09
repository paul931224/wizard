(ns wizard.editor.toolbars.view
 (:require 
  [reagent.core :as r]
  [re-frame.core :refer [dispatch subscribe]]
  [wizard.editor.toolbars.rich-text-editor.core :as rte]
  [wizard.editor.toolbars.components.core :as components]
  [wizard.editor.toolbars.order.core :as order]
  [wizard.editor.toolbars.config.core :as config]
  ["react" :as react]
  ["@dnd-kit/core" :refer [useDraggable useDroppable DndContext]]
  ["@dnd-kit/utilities" :refer [CSS]]
  ["@dnd-kit/modifiers" :refer [restrictToWindowEdges]]))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def dnd-context (r/adapt-react-class DndContext))

(defn to-clj-map [hash-map]
  (js->clj hash-map :keywordize-keys true))




;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Window types
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn components-window []
  [:div [components/view]])

(defn order-window []
  [:div [order/view]])

(defn config-window []
  [:div [config/view]])

(defn rte-window []
  (let [value-path (subscribe [:db/get [:editor :selected :value-path]])
        content?   (fn [] (contains? @(subscribe [:db/get @value-path]) :content))]
     (if (content?)
        ^{:key @value-path}[rte/view {:value-path (vec (conj @value-path :content))}])))



;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Draggable implementation
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
       use-draggable         (to-clj-map (useDraggable (clj->js {:id id})))
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
   (let [{:keys [active over]} (to-clj-map event)
         id      (:id active)] 
       (dispatch [:db/set [:editor :toolbar  :dragged] id])))
                                  

(defn handle-drag-end [event] 
  (let [{:keys [active over]} (to-clj-map event)
         id      (:id active)] 
    (dispatch [:db/set [:editor :toolbar  :dragged] nil])
    (dispatch [:db/set [:editor :toolbar  :active] id])))

(defn handle-drag-move [event]
  (let [{:keys [active over]} (to-clj-map event)
        id      (:id active)
        new-pos (-> active :rect :current :translated)
        top-and-left (select-keys new-pos [:top :left])]
    (dispatch [:db/set [:editor :toolbars id] top-and-left])))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Toolbar view
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn toolbar [config]
 [:f> draggable config])

(defn toolbars [content]
 [:div#toolbars {:style {:position :fixed 
                         :top 0}} 
  [dnd-context {:onDragStart   handle-drag-start
                :onDragMove    handle-drag-move
                :onDragEnd     handle-drag-end
                :modifiers     [restrictToWindowEdges]}
               content]])             

(defn view []
 [toolbars 
   [:<> 
     [toolbar {:id "order-window"       
               :component [order-window]
               :label "Order"}]
     [toolbar {:id "rte-window"  
               :component [rte-window]         
               :label "Rich Text Editor"}]
     [toolbar {:id "components-window"  
               :component [components-window]    
               :label "Components"}]
     [toolbar {:id "config-window"  
               :component [config-window]    
               :label "Config"}]]])