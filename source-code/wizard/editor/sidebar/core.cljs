(ns wizard.editor.sidebar.core
  (:require  [re-frame.core :refer [dispatch subscribe]]
             [plugins.drag-and-drop :as dnd]))




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
    (map-indexed 
      (fn [index a] (assoc {} (str (random-uuid)) {:type :block 
                                                   :position index
                                                   :content "Grid Block"}))
      elements)))
 

(defn grid-block []
 (let [grid-cols [2 3 4]
       grid-rows [2 3 4]
       grid-elements (range (* 
                             (count grid-rows)
                             (count grid-cols)))] 
  [component-block {:type "grid"
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
  [component-block {:type "block"
                    :name "Block"
                    :width 30
                    :height 10
                    :content "Block text"}]
  [component-block {:type "plain"
                    :name "Plain"
                    :width 30
                    :height 10
                    :content "Plain text"}]
  [component-block {:type "navbar"
                    :name "Navbar"
                    :height 3}]
  [:h3 "Relative"]
  [grid-block]])


(defn component-label [label]
 [:span {:style {:background "#333" :color "#ddd" 
                 :padding "2px"}}
  (str label)])

(defn dnd-component [props]
  [:div (str (:type props) " - " (:position props) " - " (:content props))])

(defn component-hierarchy [component-data path]
 (let [components (:components component-data)
       this-name (:name component-data)
       this-type (:type component-data)
       path-depth (count path)]
   [:div {:style {:margin-left (str (* path-depth 10) "px")}}
    [dnd/view {:value-path (vec (concat [:editor] (conj path :components)))
               :component-data component-data
               :component      component-hierarchy}]]))     
 
  
 

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