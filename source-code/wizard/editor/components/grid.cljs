(ns wizard.editor.components.grid)

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
                                                         :content "Grid Block"}))
            elements)))

(defn default []
  (let [cols {0 "1fr" 1 "2fr"}
        rows {0 "1fr" 1 "2fr" 2 "1fr"}
        grid-elements (range (*
                               (count rows)
                               (count cols)))]
     {:type "grid"
      :name "Grid"
      :cols       cols
      :rows       rows
      :components (generate-default-blocks grid-elements)
      :height 20
      :grid-padding 20
      :grid-background "#EEE"}))


(defn view [comp-router comp-state path]
  (let [the-key          (first comp-state)
        value            (second comp-state)
        rows        (:rows     value)
        cols     (:cols     value)
        gap      (:gap value)
        grid-components  (:components    value)]
    [:div.grid
     {:style {:display :grid
              :grid-template-columns (map->grid-template cols)
              :grid-template-rows    (map->grid-template rows)
              :pointer-events "auto"
              :justify-items :center
              :gap (str gap "px")}}
     ;(str path " - "  (vec (concat path [:components (first comp-state)])))
     (map (fn [component]
            ^{:key the-key} [comp-router component
                             (vec (concat path [:components (first component)]))])
          (sort-by (fn [a] (:position (second a))) grid-components))]))