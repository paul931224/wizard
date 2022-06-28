(ns wizard.resizeable.view
 (:require 
   [reagent.core :as r]
   [re-frame.core :refer [dispatch subscribe]]
   ["react" :as react]
   ["@dnd-kit/core" :refer [useDraggable useDroppable DndContext]]
   ["@dnd-kit/utilities" :refer [CSS]]
   ["@dnd-kit/modifiers" :refer [restrictToWindowEdges]]
   [wizard.utils :as utils]
   [wizard.dom-utils :as dom-utils]))


(def dnd-context (r/adapt-react-class DndContext))

(def resize-atom (r/atom {:top 0
                          :left 0
                          :height 50
                          :width 100}))

(defn get-overlapping-areas [this-area areas]
 (vec 
  (filter 
   (fn [area] 
     (let [this-top    (:top    this-area)
           this-bottom (:bottom this-area)
           this-left   (:left   this-area)
           this-right  (:right  this-area)
           area-top    (:top    (second area))
           area-bottom (:bottom (second area))
           area-left   (:left   (second area))
           area-right  (:right  (second area))]
      (or (and (>= this-bottom area-top)
               (<= this-top    area-bottom))
          (and (>= this-left   area-right)
               (<= this-right  area-left)))))
   areas)))
 

(defn resizeable-item [resize-data label component id]
  (let [ref (atom nil)
        area-dropzones  (subscribe [:db/get [:area-dropzones]])
        areas-overlapped (atom [])
        style {:style {:font-weight :bold
                       :width "100%"
                       :cursor :resize                                             
                       :height "100%"}}]
    (r/create-class 
      {:component-did-mount #(.log js/console 
                              "oi -"
                              (get-overlapping-areas
                               (dom-utils/get-rect-data @ref)
                               @area-dropzones))
       :component-did-update #(.log js/console
                                    "ui -"
                                    (get-overlapping-areas
                                     (dom-utils/get-rect-data @ref)
                                     @area-dropzones))                        
       :reagent-render 
        (fn [resize-data label component id] 
         [:div.resizeable-area 
          (merge style
                 {:ref (fn [e] (reset! ref e))
                  :on-click #(dispatch [:db/set [:editor :toolbar :active] id])})
          [:div {:style {:padding "10px"}}
           component]])})))


                        
                        
(defn drop-zone [{:keys [id]}]
 (let [ref (r/atom nil)] 
  (r/create-class
    {:component-did-mount  (fn [e] (dispatch [:db/set [:area-dropzones id] (dom-utils/get-rect-data @ref)]))
     :component-did-update (fn [e] (dispatch [:db/set [:area-dropzones id] (dom-utils/get-rect-data @ref)]))
     :reagent-render 
     (fn [props]
       [:div {:style {:height "100px"
                      :width  "400px"
                      :padding "10px"
                      :background :beige}
              :ref (fn [e] (reset! ref e))}        
          
        id])})))

(defn draggable [props]
  (let [id                    (:id props)
        label                 (:label props)
        component             (:component props)
        use-draggable         (utils/to-clj-map (useDraggable (clj->js {:id id})))
        {:keys [attributes
                listeners
                setNodeRef]}  use-draggable]
       
       
    [:div (merge {:style {:position :absolute
                          :height (str (:height @resize-atom) "px")
                          :width  (str (:width  @resize-atom)  "px")}
                  :class ["area"]                          
                  :ref (js->clj setNodeRef)}                 
                 attributes
                 listeners)
     [resizeable-item @resize-atom label component id]]))
     
     



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
        left-delta     (- (:left  old-directions) (:left  new-directions))
        top-delta      (- (:top   old-directions) (:top   new-directions))]           

    (if (contains? old-directions :right)
      (reset! resize-atom (merge @resize-atom 
                           {:width  (- (:width @resize-atom) left-delta)
                            :height (- (:height @resize-atom) top-delta)
                            :top    (- (:top  @resize-atom)   top-delta)
                            :left   (- (:left @resize-atom) left-delta)}))                                                                                          
      (reset! resize-atom (merge @resize-atom new-directions)))))
                                       
   

(defn area [config]
  [:f> draggable config])

(defn one-area []
  [:div
   "Look at me MeeSeeks"])

(defn areas []
  [:div#toolbars {:style {:position :fixed
                          :top 0
                          :z-index 100}}

    [dnd-context {:onDragStart   handle-drag-start
                  :onDragMove     handle-drag-move
                  :onDragEnd      handle-drag-end}
                  ;:modifiers      [restrictToWindowEdges]}
     [area {:id "a"
            :component [one-area]
            :label     "Area a"}]
     [:div {:style {:height "100px"}}]
     [:div {:style {:height "100px"}}]
     [drop-zone {:id "area-b"}]
     [drop-zone {:id "area-c"}]]])


(defn view []
 (let [] 
   [:div {:style {:position :relative}}     
     [areas]]))
      
     