(ns wizard.editor.components.grid)


(defn generate-default-blocks [elements]
  (reduce merge
           (map-indexed
            (fn [index a] (assoc {} (str (random-uuid)) {:type :block
                                                         :position index
                                                         :content "Grid Block"}))
            elements)))

(defn default []
  (let [grid-cols [2 3 4]
        grid-rows [2 3 4]
        grid-elements (range (*
                               (count grid-rows)
                               (count grid-cols)))]
     {:type "grid"
      :name "Grid"
      :grid-columns grid-cols
      :grid-rows    grid-rows
      :components (generate-default-blocks grid-elements)
      :height 20
      :grid-padding 20
      :grid-background "#EEE"}))

(defn grid-fractions [numbers-vec]
  (str
   (clojure.string/join "fr " numbers-vec) "fr"))

(defn view [comp-router comp-state path]
  (let [the-key          (first comp-state)
        value            (second comp-state)
        grid-rows        (:grid-rows     value)
        grid-columns     (:grid-columns  value)
        grid-components  (:components    value)
        grid-padding     (:grid-padding  value)
        grid-background  (:grid-background  value)]
    [:div.grid
     {:style {:display :grid
              :grid-template-columns (grid-fractions grid-columns)
              :grid-template-rows    (grid-fractions grid-rows)
              :pointer-events "auto"
              :justify-items :center
              :gap "10px"
              :padding (str grid-padding "px")
              :background grid-background}}
     ;(str path " - "  (vec (concat path [:components (first comp-state)])))
     (map (fn [component]
            ^{:key the-key} [comp-router component
                             (vec (concat path [:components (first component)]))])
          (sort-by (fn [a] (:position (second a))) grid-components))]))