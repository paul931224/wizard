(ns wizard.editor.toolbars.view
 (:require 
  [reagent.core :as r]
  [re-frame.core :refer [dispatch subscribe]]
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
       {:keys [attributes listeners setNodeRef  transform]} 
       (to-clj-map (useDraggable (clj->js {:id id})))
       moved-style (subscribe [:db/get [:editor :toolbars id]])]
      [:div (merge 
             {:ref (js->clj setNodeRef)
              :style 
                 (merge 
                   {:cursor :pointer
                    
                    :transform (.toString (.-Transform CSS) (clj->js transform))}
                   draggable-window-style
                   @moved-style)}
             listeners 
             attributes)       
       "Drag handle: "]))


(defn view []
 (let [handle-drag-end (fn [event]
                         (let [{:keys [active over]} (to-clj-map event)
                               id      (:id active)
                               new-pos (-> active :rect :current :translated)
                               top-and-left (select-keys new-pos [:top :left])]
                           (dispatch [:db/set [:editor :toolbars id] top-and-left])))]
                                                                                     
  [:div 
   [dnd-context {:onDragEnd     handle-drag-end
                 :modifiers     [restrictToWindowEdges]}
                [:f> draggable {:id "draggable-1"}]]]))
   