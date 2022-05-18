(ns wizard.editor.components
  (:require  [re-frame.core :refer [dispatch subscribe]]
             [wizard.editor.config :refer [row-height col-width]]
             [wizard.editor.rich-text-editor.core :as rtf]
             [reagent-hickory.sweet :refer [html->hiccup]]))





(defn plain [key-and-comp]
 (let [the-key                        (first key-and-comp)
       comp-state                     (second key-and-comp)
       {:keys [content col row width height]}  comp-state]
   [:div {:style {:position :absolute
                  :top    (str (* row row-height) "px")
                  :left   (str (* col col-width) "px")
                  :width  (str (* width  col-width) "px")
                  :height (str (* height row-height) "px")
                  :background :white 
                  :pointer-events "auto"}}         
      [:div.component (html->hiccup content)]]))
      ;[rtf/view {:value-path [:editor :components the-key :content]}]]))
      

(defn navbar [key-and-comp]
 (let [comp-state                     (second key-and-comp)
       {:keys [height]}  comp-state]
   [:div {:style {:position  :relative 
                  :width     "100%" 
                  :background "rgba(0,0,0,0.3)"
                  :border-bottom-left-radius "10px"
                  :border-bottom-right-radius "10px"
                  :height    (str (* height row-height) "px")
                  :display :flex 
                  :align-items :center 
                  :pointer-events "auto"}}  
     [:h2 {:style {:padding-left (str (* 1 col-width) "px")}} 
      "Navbar"]]))       


(defn grid-fractions [numbers-vec]
 (str 
  (clojure.string/join "fr " numbers-vec) "fr"))

(defn grid [comp-state]
 (let [grid-rows    (:grid-rows    (second comp-state))
       grid-columns (:grid-columns (second comp-state))]
   [:div.grid 
     {:style {:display :grid
              :grid-template-columns (grid-fractions grid-columns)
              :grid-template-rows    (grid-fractions grid-rows)
              :pointer-events "auto"}}
     [:div "dslajdlksajldkasjldkj"
      [:div "oi"]]
     [:div 2]
     [:div 3]
     [:div 4]
     [:div 5]
     [:div 6]
     [:div 7]]))

(defn component-router [comp-state]
 (let [type (:type (second comp-state))]
  (case type 
   :plain  [plain  comp-state]
   :navbar [navbar comp-state]
   :grid   [grid   comp-state]
   [plain comp-state])))
 

(defn view []
 (let [components (subscribe [:db/get [:editor :components]])] 
   [:div {:style {:position :absolute
                  :pointer-events :none          
                  :width "100%"
                  :height "100%"}}  
     [:<> 
        (map
         (fn [comp-state] ^{:key (first comp-state)}[component-router comp-state])
         @components)]]))
            
