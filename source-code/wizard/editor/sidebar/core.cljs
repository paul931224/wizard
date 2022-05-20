(ns wizard.editor.sidebar.core
  (:require  [re-frame.core :refer [dispatch subscribe]]))




(defn component-block [{:keys [name] :as component-data}]
 (let [pos (subscribe [:db/get [:editor :selected-particle]])]
  [:div.wizard-component 
    {:on-click (fn [e] 
                (dispatch [:editor/add! (merge component-data @pos)])
                (dispatch [:animation/close-sidebar!]))
     :style {:color "#333" 
             :cursor :pointer
             :padding "5px"
             :border-radius "5px"
             :background "white"}}
    [:h3 name]]))
    

(defn generate-blocks [elements]
 (reduce merge 
    (map 
      (fn [a] (assoc {} (str (random-uuid)) {:type :block 
                                             :content "Grid Block"}))
      elements)))
 

(defn grid-block []
 (let [grid-cols [2 3 4]
       grid-rows [2 3 4]
       grid-elements (range (* 
                             (count grid-rows)
                             (count grid-cols)))] 
  [component-block {:type :grid
                    :name "Grid"
                    :grid-columns grid-cols
                    :grid-rows    grid-rows
                    :components (generate-blocks grid-elements)
                    :height 20
                    :grid-padding 20
                    :grid-background "#EEE"
                    :content "Plain text"}]))

(defn elements []
 [:div
  [:h3 "Absolute"]
  [component-block {:type :plain
                    :name "Plain"
                    :width 30
                    :height 10
                    :content "Plain text"}]
  [component-block {:type :navbar
                    :name "Navbar"
                    :height 3}]
  [:h3 "Relative"]
  [grid-block]])


(defn component-label [label]
 [:span {:style {:background "#333" :color "#ddd" 
                 :padding "2px"}}
  (str label)])

(defn component-hierarchy [component-data path]
 (let [components (:components component-data)
       this-name (:name component-data)
       this-type (:type component-data)
       path-depth (count path)]
   [:div {:style {:margin-left (str (* path-depth 10) "px")}}
    [:div {:style {:margin-top "10px"
                   :background :white 
                   :padding "10px 5px"
                   :color "#222"
                   :display :flex 
                   :justify-content :space-between}}
          [component-label (str this-type)]
          [:img {:src "/images/arrow-down-icon.png" 
                 :width "25px"
                 :height "25px"}]]
    (map
     (fn [component]
        (let [component-key    (first component)
              component-value  (second component)]
          [component-hierarchy 
             component-value 
             (vec (concat path [:components component-key]))]))            
     components)]))     
 
  

(defn sidebar []
 (let [editor (subscribe [:db/get [:editor]])]
  [:div 
    [component-hierarchy @editor []]
    [elements]]))
   
(defn view []
 [:div 
  [:div#sidebar-container 
    {:on-click (fn [e] (dispatch [:animation/close-sidebar!]))
     :style {:position :fixed 
             :top 0 
             :cursor :pointer
             :left 0 
             
             :height "100vh"
             :width  "100vw"
             :display :none 
             :z-index 95}}] 
  [:div#sidebar {:style {:position :fixed 
                         :top 0 
                         :right "-100%" 
                         :z-index 100 
                         :color :white
                         :width "500px"
                         :height "100vh"
                         :overflow-y :scroll 
                         :background "#333" 
                         :padding "10px"}} 
    [sidebar]]])