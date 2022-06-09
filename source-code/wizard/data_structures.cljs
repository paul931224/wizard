(ns wizard.data-structures)


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