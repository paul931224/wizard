(ns wizard.toolbars.view
 (:require 
  [reagent.core :as r]
  [re-frame.core :refer [dispatch subscribe]]
  [wizard.toolbars.rich-text-editor.core :as rte]
  [wizard.toolbars.components.core :as components]
  [wizard.toolbars.config.core :as config]
  ["react" :as react]
  ["@dnd-kit/core" :refer [useDraggable useDroppable DndContext]]
  ["@dnd-kit/utilities" :refer [CSS]]
  ["@dnd-kit/modifiers" :refer [restrictToWindowEdges]]
  [wizard.utils :as utils]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def dnd-context (r/adapt-react-class DndContext))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Window types
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn components-window []
  [:div [components/view]])


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
  [:div {:on-click #(dispatch [:db/set [:toolbars :active] id])
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
       dragged-id            (subscribe [:db/get [:toolbars  :dragged]])
       active-id             (subscribe [:db/get [:toolbars  :active]])
       use-draggable         (utils/to-clj-map (useDraggable (clj->js {:id id})))
       {:keys [attributes 
               listeners
               setNodeRef  
               transform]}   use-draggable 
       moved-style           (subscribe [:db/get [:toolbars :positions id]])
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
       (dispatch [:db/set [:toolbars  :dragged] id])))
                                  

(defn handle-drag-end [event] 
  (let [{:keys [active over]} (utils/to-clj-map event)
         id      (:id active)] 
    (dispatch [:db/set [:toolbars  :dragged] nil])
    (dispatch [:db/set [:toolbars  :active] id])))

(defn handle-drag-move [event]
  (let [{:keys [active over]} (utils/to-clj-map event)
        id      (:id active)
        new-pos (-> active :rect :current :translated)
        top-and-left (select-keys new-pos [:top :left])]
    (dispatch [:db/set [:toolbars :positions id] top-and-left])))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Toolbar view
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn toolbar [config]
 [:f> draggable config])

(defn toolbars [content]
 [:div#toolbars {:style {:position :fixed 
                         :top 0
                         :z-index 100}} 

  [dnd-context {:onDragStart   handle-drag-start
                :onDragMove    handle-drag-move
                :onDragEnd     handle-drag-end
                :modifiers     [restrictToWindowEdges]}
               content]])             

(defn view []
 (let [selected-element-type (:type @(subscribe [:editor/get-selected-component]))]
  [toolbars 
     [:<> 
       (if (= "block" selected-element-type)
           [toolbar {:id "rte-window"  
                     :component [rte-window]         
                     :label "Rich Text Editor"}])          
       [toolbar {:id "components-window"  
                     :component [components-window]    
                     :label "Components"}]
       [toolbar {:id "config-window"  
                     :component [config-window]    
                     :label "Config"}]]]))