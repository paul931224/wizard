(ns wizard.overlays.grid
   (:require [re-frame.core :refer [subscribe dispatch]]
             [wizard.overlays.wrapper :as overlay-wrapper]
             [wizard.editor.grid :as grid]
             [wizard.utils :as utils]))
            
          

(defn get-row-index [col-count index]
  (if (= 0 (rem (inc index) col-count))
    (dec (quot (inc index) col-count))
    nil))

(defn row-indicator? [col-count index]
  (= 0 (rem (inc index) col-count)))

(defn col-indicator? [col-count index]
 (< index col-count))

(defn row-indicator []
  [:div
   {:style {:cursor     :pointer 
            :position   :absolute
            :bottom     "0px"
            :right      "-10px"
            :background :blue
            :height     "5px"
            :width      "10px"}}])

(defn col-indicator []
 [:div
  {:style {:cursor      :pointer
           :position    :absolute
           :top         "-10px"
           :right       0
           :background  :red
           :height      "10px"
           :width       "5px"}}])


(defn rem-col-indicator []
  [:div
   {:on-click  #(dispatch [:grid/rem-col!])
    :style {:cursor :pointer            
            :background :lightgreen
            :color :black
            :padding :5px
            :border-radius :5px}}
   "-"])

(defn add-col-indicator []
  [:div
   {:on-click  #(dispatch [:grid/add-col!])
    :style {:cursor :pointer            
            :background :lightgreen
            :color :black
            :padding :5px
            :border-radius :5px}}                    
   "+"])

(defn modify-col []
 [:div {:style {:display :flex 
                :position :absolute
                :top   "-50px"
                :right "-70px"}}
  [rem-col-indicator]
  [:div {:style {:width "5px"}}]
  [add-col-indicator]])   

(defn rem-row-indicator []
  [:div
   {:on-click  #(dispatch [:grid/rem-row!])
    :style {:cursor :pointer
            :background :lightgreen
            :color :black
            :padding :5px
            :border-radius :5px}}
   "-"])

(defn add-row-indicator []
  [:div
   {:on-click  #(dispatch [:grid/add-row!])
    :style {:cursor :pointer
            :background :lightgreen
            :color :black
            :padding :5px
            :border-radius :5px}}         
   "+"])                     

(defn modify-row []
  [:div {:style {:display   :flex 
                 :position  :absolute
                 :bottom    "-50px"
                 :right     "-70px"}}
   [rem-row-indicator]
   [:div {:style {:width "5px"}}]
   [add-row-indicator]])

(defn col-modifier [index value-path]
  (let [col-val-path (fn [] (vec (concat value-path [:cols index])))
        col-data     (fn [] (str @(subscribe [:db/get (col-val-path)])))]
    (fn [index value-path]
      [:input {:style       {:position :absolute
                             :top "-40px"
                             :left "50%"
                             :transform "translateX(-50%)"
                             :width "50px"
                             :text-align :center}
               :value       (col-data)               
               :on-change   (fn [e] (dispatch [:db/set (col-val-path) (-> e .-target .-value)]))}])))
 
(defn row-modifier [index value-path]
  (let [row-val-path (fn [] (vec (concat value-path [:rows index])))
        row-data     (fn [] @(subscribe [:db/get (row-val-path)]))]
    [:input {:style {:position    :absolute
                     :right       "-80px"
                     :width       "50px"
                     :top         "50%"
                     :transform "translateY(-50%)"
                     :text-align  :center}
             :value  (str (row-data))
             :on-change    (fn [e] (dispatch [:db/set (row-val-path) (-> e .-target .-value)]))}]))

(defn grid-item [index grid-data value-path]
 (let [cols      (:cols grid-data) 
       rows      (:rows grid-data)  
       col-count (count cols)
       row-count (count rows)
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
    index
    (if (col-indicator? col-count index)
     [:<>
      [col-modifier index value-path]
      [col-indicator]])
    (if (row-indicator? col-count index)
      [:<> 
       [row-modifier (get-row-index col-count index) value-path]
       [row-indicator]])
    (if (= add-col-index index)
     [modify-col])
    (if (= add-row-index index)
     [modify-row])]]))

  

(defn view []
 (let [overlay-type          (fn [] @(subscribe [:db/get [:overlays :type]]))
       value-path            (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))
       components-value-path (fn [] (vec (conj (value-path) :components)))
       grid-data             (fn [] @(subscribe [:db/get (value-path)]))  
       components            (fn [] @(subscribe [:db/get (components-value-path)]))     
       col-count             (fn [] (count (:cols (grid-data))))
       row-count             (fn [] (count (:rows (grid-data))))
       items                 (fn [] (range (* (col-count) (row-count))))]
                                
  (if (= :grid (overlay-type))
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
            (map-indexed (fn [index item] [grid-item index (grid-data) (value-path)])
                         (items))
            (last (value-path)) 
            @(subscribe [:db/get (value-path)])]]])))