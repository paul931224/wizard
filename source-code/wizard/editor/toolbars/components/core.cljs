(ns wizard.editor.toolbars.components.core
 (:require [re-frame.core :refer [dispatch subscribe]]))
  

(defn component-block [{:keys [name] :as component-data}]
  (let [pos (subscribe [:db/get [:editor :selected-particle]])]
     [:div.wizard-component
      {:on-click (fn [e]
                    (.log js/console "hello")
                    (dispatch [:editor/add! (merge component-data @pos)]))}
     
      name]))


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
                       :grid-background "#EEE"}]))
                       

(defn view []
  [:div {:style {:padding "0px 5px"}}
   [component-block {:type "block"
                     :name "Block"
                     :width 30
                     :height 10
                     :content "Block text"}]
   [component-block {:type "navbar"
                     :name "Navbar"
                     :height 3}]   
   [grid-block]])