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

(def resize-atom (r/atom {:top 0
                          :left 0
                          :height 50
                          :width 100}))

(defn resizeable-item [label component id]
  (let [style {:style {:font-weight :bold
                       
                       :cursor :resize                                             
                       :height "100%"}}]
    [:div.resizeable-area 
     (merge style
            {:on-click #(dispatch [:db/set [:editor :toolbar :active] id])})
     [:div {:style {:padding "10px"}}
      component]]))


                        
                        


(defn draggable [props]
  (let [id                    (:id props)
        label                 (:label props)
        component             (:component props)
        use-draggable         (utils/to-clj-map (useDraggable (clj->js {:id id})))
        {:keys [attributes
                listeners
                setNodeRef]}  use-draggable]
       
       
    [:div (merge {:style {:position :relative
                          :height (str (:height @resize-atom) "px")
                          :width  (str (:width  @resize-atom)  "px")}
                  :class ["area"]                          
                  :ref (js->clj setNodeRef)}                 
                 attributes
                 listeners)
     [resizeable-item label component id]]))
     
     



(defn handle-drag-start [event]
  (let [{:keys [active over]} (utils/to-clj-map event)
        id      (:id active)]
    (dispatch [:db/set [:editor :toolbar  :dragged] id])))


(defn handle-drag-end [event]
  (let [{:keys [active over]} (utils/to-clj-map event)
        id      (:id active)] 
    (reset! resize-atom (dissoc @resize-atom :bottom :top :right :left))
    (dispatch [:db/set [:area-editor :dragged] nil])
    (dispatch [:db/set [:area-editor :active] id])))

(defn handle-drag-move [event]
  (let [{:keys [active over]} (utils/to-clj-map event)
        id      (:id active)
        new-pos (-> active :rect :current :translated)
                                           
        old-directions (select-keys @resize-atom [:bottom :top
                                                  :left   :right])                            
        new-directions (select-keys new-pos [:bottom :top
                                             :left   :right])
        left-delta     (- (:left  old-directions) (:left  new-directions))]           
    
    (if (contains? old-directions :right)
      (do 
        (.log js/console (str "hello: ")
                      "old: "  (int (:left old-directions)) 
                      " - " 
                      "new: "  (int (:left new-directions)) 
                      " - " 
                      "delta:" (int left-delta))                                        
        (reset! resize-atom (merge @resize-atom 
                              {:width (- (:width @resize-atom) left-delta)
                               :left (- (:left @resize-atom) left-delta)})))                                                                                          
      (reset! resize-atom (merge @resize-atom new-directions)))))
                                       
   

(defn toolbar [config]
  [:f> draggable config])

(defn toolbars [content]
  [:div#toolbars {:style {:position :fixed
                          :top 0
                          :z-index 100}}

    [dnd-context {:onDragStart   handle-drag-start
                  :onDragMove     handle-drag-move
                  :onDragEnd      handle-drag-end}
                  ;:modifiers      [restrictToWindowEdges]}
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