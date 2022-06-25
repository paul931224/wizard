(ns wizard.overlays.grid
   (:require [re-frame.core :refer [subscribe dispatch]]
             [wizard.overlays.wrapper :as overlay-wrapper]
             [wizard.editor.grid :as grid]
             [wizard.utils :as utils]))


(defn row-indicator? [col-count index]
  (= 0 (rem (inc index) col-count)))

(defn col-indicator? [col-count index]
 (< index col-count))

(defn row-indicator []
  [:div
   {:style {:cursor :pointer 
            :position :absolute
            :bottom "0px"
            :right  "-10px"
            :background :blue
            :height "5px"
            :width  "10px"}}])

(defn col-indicator []
 [:div
  {:style {:cursor :pointer
           :position :absolute
           :top "-10px"
           :right 0
           :background :red
           :height "10px"
           :width  "5px"}}])


(defn get-grid-component []
  @(subscribe [:editor/get-selected-component]))

(defn add-to-component-path [path-vec]
  (vec (concat @(subscribe [:editor/get-selected-component-path]) path-vec)))

(defn get-map-length [the-map]
  (count the-map))

(defn rem-col []
  (let [cols        (subscribe [:db/get (add-to-component-path [:cols])])
        last-index  (max 1 (dec (get-map-length @cols)))]
    (dispatch [:db/set (add-to-component-path [:cols]) (dissoc @cols last-index)])))

(defn rem-row []
  (let [rows       (subscribe [:db/get (add-to-component-path [:rows])])
        last-index (max 1 (dec (get-map-length @rows)))]
    (dispatch [:db/set (add-to-component-path [:rows]) (dissoc @rows last-index)])))

(defn add-col []
  (let [cols        (subscribe [:db/get (add-to-component-path [:cols])])
        next-index  (get-map-length @cols)]
    (dispatch [:db/set (add-to-component-path [:cols next-index]) "1fr"])))

(defn add-row []
  (let [rows       (subscribe [:db/get (add-to-component-path [:rows])])
        next-index (get-map-length @rows)]
    (dispatch [:db/set (add-to-component-path [:rows next-index]) "100px"])))


(defn rem-col-indicator []
  [:div
   {:on-click  rem-col
    :style {:cursor :pointer            
            :background :lightgreen
            :color :black
            :padding :5px
            :border-radius :5px}}
   "-"])

(defn add-col-indicator []
  [:div
   {:on-click  add-col
    :style {:cursor :pointer            
            :background :lightgreen
            :color :black
            :padding :5px
            :border-radius :5px}}                    
   "+"])

(defn modify-col []
 [:div {:style {:display :flex 
                :position :absolute
                :top "-50px"
                :right 0}}
  [rem-col-indicator]
  [:div {:style {:width "5px"}}]
  [add-col-indicator]])   

(defn rem-row-indicator []
  [:div
   {:on-click  rem-row
    :style {:cursor :pointer
            :background :lightgreen
            :color :black
            :padding :5px
            :border-radius :5px}}
   "-"])

(defn add-row-indicator []
  [:div
   {:on-click  add-row
    :style {:cursor :pointer
            :background :lightgreen
            :color :black
            :padding :5px
            :border-radius :5px}}         
   "+"])                     

(defn modify-row []
  [:div {:style {:display :flex 
                 :position :absolute
                 :bottom "0px"
                 :right  "-50px"}}
   [rem-row-indicator]
   [:div {:style {:width "5px"}}]
   [add-row-indicator]])

(defn grid-item [index grid-data]
 (let [col-count (count (:cols grid-data))
       row-count (count (:rows grid-data))
       add-col-index (dec col-count)
       add-row-index (dec (* col-count row-count))] 
  [:div {:style {:background "rgba(0,0,0,0.3)"
                 :display :flex
                 :justify-content :center
                 :align-items :center
                 :color "#DDD"
                 :height "100%"
                 :width "100%"
                 :position :relative}}                
   [:div {:style {:background "#333"
                  :height "30px"
                  :width "30px"
                  :display :flex 
                  :justify-content :center
                  :align-items :center
                  :border-radius "15px"}}
    index]
   (if (col-indicator? col-count index)
    [col-indicator])
   (if (row-indicator? col-count index)
     [row-indicator])
   (if (= add-col-index index)
    [modify-col])
   (if (= add-row-index index)
    [modify-row])]))

  

(defn view []
 (let [overlay (subscribe [:db/get [:editor :overlay :type]])
       value-path            (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
       components-value-path (fn [] (vec (conj (value-path) :components)))
       grid-data             (fn [] @(subscribe [:db/get (value-path)]))  
       components            (fn [] @(subscribe [:db/get (components-value-path)]))     
       col-count (fn [] (count (:cols (grid-data))))
       row-count (fn [] (count (:rows (grid-data))))
       items     (fn [] (range (* (col-count) (row-count))))]
                                
  (if (= :grid @overlay)
   [overlay-wrapper/view 
    [:div#grid-overlay
       {:style {:height "100%"
                :width "100%"   
                :position :absolute
                :left 0
                :backdrop-filter "blur(3px)"
                :pointer-events :auto
                :z-index 2}}                                      
       [grid/grid-wrapper
            (map-indexed (fn [index item] [grid-item index (grid-data)])
                         (items))
            (vector 
                   (last (value-path)) 
                   @(subscribe [:db/get (value-path)]))]]])))