(ns wizard.utils)


(defn to-clj-map [hash-map]
  (js->clj hash-map :keywordize-keys true))

(defn id-map->ordered-vector [coll]
  (let [pos-to-index (fn [[item-id item-value]]
                       [(:position item-value)
                        (assoc item-value :id item-id)])]
    (mapv second
          (sort-by first
                   (map pos-to-index coll)))))

(defn ordered-vector->id-map [coll]
  (let [index-to-pos (fn [index item]
                       {(str (:id item))
                        (-> item
                            (dissoc :id)
                            (assoc  :position index))})]
    (reduce merge (map-indexed index-to-pos coll))))

(defn number-to-letter [index]
  (clojure.string/lower-case (str (char (+ 65 index)))))



(defn generate-abc-matrix [how-many]
  (let [numbers (range how-many)]
     (vec (map number-to-letter numbers))))

(defn randomize-rgb [a]
  (let [value-range (range 256)
        r (rand-nth value-range)
        g (rand-nth value-range)
        b (rand-nth value-range)
        a "0.5"
        rgba-code (clojure.string/join ", " [r g b a])]
    (str "rgba(" rgba-code ")")))

(def random-colors
  (mapv randomize-rgb (range 1000)))