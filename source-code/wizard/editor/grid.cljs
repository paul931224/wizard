(ns wizard.editor.grid
 (:require [wizard.utils :as utils]))

(defn grid-divs-range [block]
  (let [grid-col-count (inc (count (:cols block)))
        grid-row-count (inc (count (:rows block)))]
     (range (* grid-col-count grid-row-count))))

(defn map->grid-template [the-map]
  (clojure.string/join " " (vals the-map)))


(defn generate-default-blocks [elements]
  (reduce merge
           (map-indexed
            (fn [index a] (assoc {} (str (random-uuid)) {:type "block"
                                                         :position index
                                                         :content (str index " - block")}))                                                       
            elements)))

(defn default []
  (let [cols {0 "30%" 1 "70%"}
        rows {0 "100px" 1 "200px" 2 "100px" 3 "75px"}
        grid-elements (range 5)]
     {:type "grid"
      :name "Grid"
      :cols       cols
      :rows       rows
      :components (generate-default-blocks grid-elements)
      :height 20
      :grid-padding 20
      :grid-background "#EEE"}))


(defn grid-wrapper 
 ([comp-state]
  [grid-wrapper nil comp-state])
 ([content comp-state]
  (let [the-key          (first  comp-state)
        value            (second comp-state)
        rows             (:rows     value)
        cols             (:cols     value)]                
    [:div.grid
      {:id    (str "grid-" the-key)
       :style {:display :grid
               :grid-template-columns (map->grid-template cols)
               :grid-template-rows    (map->grid-template rows)
               :pointer-events "auto"
               :justify-items :center
               :grid-template-areas "\"a b\" 
                                     \"a b\"
                                     \"c c\"
                                     \"d e\""
               ;:background :black               
               :gap "1px"}}     
      content])))  

(defn view [comp-router comp-state path]
  (let [the-key          (first comp-state)
        value            (second comp-state)
        grid-components  (:components    value)]
   [grid-wrapper
      (map (fn [component]
             ^{:key the-key} [comp-router component
                              (vec (concat path [:components (first component)]))])
           (sort-by (fn [a] (:position (second a))) grid-components))
      comp-state]))