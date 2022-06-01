(ns wizard.editor.toolbars.config.grid-customizer
 (:require [reagent.core :refer [atom]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def col-count (atom 1))
(def row-count (atom 1))

(def grid 
 {:rows {0 "1fr"} 
  :cols {0 "1fr"}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; State
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



(defn rem-col []
  (reset! col-count (max 0 (dec @col-count))))

(defn rem-row []
  (reset! row-count (max 0 (dec @row-count))))

(defn add-col []
  (reset! col-count (inc @col-count)))

(defn add-row []
  (reset! row-count (inc @row-count)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Utils
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(def button-style
  {:border "1px solid white"
   :border-radius "10px"
   :font-weight :bold
   :padding "5px 10px"
   :text-align :center
   :margin "5px"
   :cursor :pointer})


(defn add-col-button []
  [:div {:style     button-style
         :on-click  add-col}
    "+ col"])

(defn add-row-button []
  [:div {:style     button-style
         :on-click  add-row}
    "+ row"])

(defn rem-col-button []
  [:div {:style     button-style
         :on-click  rem-col}
    "- col"])

(defn rem-row-button []
  [:div {:style     button-style
         :on-click  rem-row}
    "- row"])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; View
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn grid-div
 ([] [grid-div ""])
 ([content] 
  [:div {:style {:outline "1px solid #ddd"
                 :min-width "100px"}} 
   content]))

(defn grid-preview []
 [:div {:style {:display :grid
                :height "300px"
                :width "100%"
                :gap "10px"
                :grid-template-columns "2fr 1fr 1fr 1fr"
                :grid-template-rows    "1fr minmax(100px, 2fr)"
                :grid-auto-rows        "minmax(100px, auto)"
                :grid-auto-columns     "minmax(100px, auto)"}}
       
      [grid-div]
      [grid-div]
      [grid-div]
      [grid-div]
      [grid-div]
      [grid-div]])

(defn grid-buttons []
 [:div {:style {:display :grid 
                :grid-template-columns "1fr 1fr"}}
  [add-row-button]
  [rem-row-button]

  [add-col-button]
  [rem-col-button]])
  

(defn view []
  [:div
    "Grid customizer"
    [grid-buttons]
    [grid-preview]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; View
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
   