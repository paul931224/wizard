(ns wizard.toolbars.config.grid-customizer
 (:require [reagent.core :as reagent :refer [atom]]
           [wizard.editor.components.grid :as grid]          
           [re-frame.core :refer [dispatch subscribe]]))
           

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
   (dispatch [:db/set (add-to-component-path [:cols]) (dissoc @rows last-index)])))

(defn add-col []
  (let [cols        (subscribe [:db/get (add-to-component-path [:cols])])
        next-index  (get-map-length @cols)]
   (dispatch [:db/set (add-to-component-path [:cols next-index]) "1fr"])))

(defn add-row []
  (let [rows       (subscribe [:db/get (add-to-component-path [:rows])])
        next-index (get-map-length @rows)]
   (dispatch [:db/set (add-to-component-path [:rows next-index]) "1fr"])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def button-style
  {:font-weight :bold
   :padding "0px 3px"
   :text-align :center
   :margin "5px"
   :cursor :pointer})


(defn add-col-button []
  [:div {:style     button-style
         :on-click  add-col}
    "+"])

(defn add-row-button []
  [:div {:style     button-style
         :on-click  add-row}
    "+"])

(defn rem-col-button []
  [:div {:style     button-style
         :on-click  rem-col}
    "-"])

(defn rem-row-button []
  [:div {:style     button-style
         :on-click  rem-row}
    "-"])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; View
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn grid-buttons []
 [:div {:style {:position :relative
                :top 0 
                :width "100%"
                :height "100%"}}
  [:div {:style {:position :absolute
                 :top 0 
                 :width "100%"
                 :height "100%"
                 :display :grid
                 :grid-template-rows "3fr 1fr"}}
                  
      [:div]
      [:div {:style {:display :flex :z-index 1}}
       [rem-row-button]
       [add-row-button]]]
       
       
  [:div {:style {:position :absolute
                 :top 0 
                 :width "100%"
                 :height "100%"
                 :display :grid 
                 :grid-template-columns "3fr 1fr"}} 
      [:div]
      [:div 
       [add-col-button]
       [rem-col-button]]]]) 
       
       

(defn get-offset-index [number]
 (let [grid-col-count (fn [] (inc (count @(subscribe [:db/get (add-to-component-path [:cols])]))))
       grid-row-count (fn [] (inc (count @(subscribe [:db/get (add-to-component-path [:cols])]))))
       double-col-count (fn [] (* 2 (grid-col-count)))
       row-index (fn [] (dec (quot number (grid-col-count))))
       index-without-first-row (fn [] (- number (grid-col-count)))] 
    (cond
       (= number        0)                    :index-zero
       (< number        (grid-col-count))     :top-row 
       (= 0 (mod number (grid-col-count)))    :left-col
       (<  number (double-col-count))         (index-without-first-row)
       (>=  number (double-col-count))        (- (index-without-first-row) (row-index))
       :else                                  number)))


(defn col-config [number]
 (let [col-index (fn [] (dec number))] 
  (fn [number] 
    [:div {:style {:padding-top "5px"
                    :display :flex 
                    :align-items :center
                    :justify-content :center
                    :height "100%"}}     
      [:input {:value           @(subscribe [:db/get (add-to-component-path [:cols (col-index)])])
               :on-change (fn [e] (dispatch [:db/set (add-to-component-path [:cols (col-index)]) 
                                             (-> e .-target .-value)]))
               :style {:width "50px" :margin-right "10px"}
               :placeholder 1}]])))
     

(defn row-config [number]
  (let [grid-col-count (fn [] (inc (count @(subscribe [:db/get (add-to-component-path [:cols])]))))
        row-index      (fn [] (dec (quot number (grid-col-count))))] 
   (fn [number] 
    [:div {:style {:padding-top "5px"
                   :display :flex
                   :flex-direction :column
                   :align-items :center
                   :justify-content :center
                   :height "100%"}}
      [:input {:value      @(subscribe [:db/get (add-to-component-path [:rows (row-index)])])
               :on-change (fn [e] (dispatch [:db/set (add-to-component-path [:rows (row-index)]) 
                                             (-> e .-target .-value)]))
               :style {:width "50px"
                       :margin-bottom "5px"}
               :placeholder 1}]])))
     
(defn grid-block [index]
 [:div {:style {:height "100%"
                     :width "100%"
                     :display :flex 
                     :justify-content :center 
                     :align-items :center}}
    index])


(defn grid-div [number] 
  (let [new-index (get-offset-index number)] 
    [:div {:style {:outline "1px solid #ddd"}
                 :min-width  "100px"
                 :min-height "100px"}
                                   
      (cond 
       (= :index-zero new-index) [grid-buttons]
       (= :top-row    new-index) [col-config number]
       (= :left-col   new-index) [row-config number]
       :else          [grid-block new-index])]))
         


(defn grid-preview []
 (let [cols (fn [] (:cols (get-grid-component)))
       rows (fn [] (:rows (get-grid-component)))]
   [:div {:style {:display :grid
                  :padding "20px"                 
                  :gap "10px"
                  :grid-template-columns (str "100px " (grid/map->grid-template (cols)))
                  :grid-template-rows    (str "100px " (grid/map->grid-template (rows)))
                  :grid-auto-rows        "minmax(100px, auto)"
                  :grid-auto-columns     "minmax(100px, auto)"}}
        (map-indexed 
         (fn [index a] ^{:key index}[grid-div index]) 
         (grid/grid-divs-range (get-grid-component)))]))
  
(defn add-grid []
 [:div 
  {:on-click (fn [e] (dispatch [:editor/add! @(subscribe [:db/get (add-to-component-path [])])]))
   :style {:padding "10px"
           :cursor :pointer}}
  "Add grid to page"])


(defn view []
  [:div
    ;(str @(subscribe [:db/get [:toolbars :grid]])) 
    [grid-preview]
    [add-grid]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; View
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   