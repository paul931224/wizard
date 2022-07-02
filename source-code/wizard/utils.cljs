(ns wizard.utils
 (:require  [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]))


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

(defn state-viewer--vector-display [recur-fn tree]
 [:div (str tree)])
 
(defn state-viewer--map-display-item [recur-fn the-key sub-tree]
 (let [open? (r/atom true)] 
   (fn [recur-fn the-key sub-stree] 
    [:div {:style {:display :flex}} 
         [:div {:style {:cursor :pointer}
                :on-click (fn [e] (reset! open? (not @open?)))} (str the-key)]              
         (if @open? 
          [:div {:style {:margin-left "10px"}}
            [:div {:style {:height "20px"}}] 
            [recur-fn sub-tree]])])))

(defn state-viewer--map-display [recur-fn tree]
  [:div {:style {:border-left "2px solid #881616"}}
      (map (fn [[the-key sub-tree]] 
             [state-viewer--map-display-item recur-fn the-key sub-tree])    
           tree)])

(defn state-viewer--recursion [tree]
 [:div (do (cond 
             (vector? tree) ^{:key (str (random-uuid))}[state-viewer--vector-display state-viewer--recursion tree]
             (map? tree)    ^{:key (str (random-uuid))}[state-viewer--map-display    state-viewer--recursion tree]
             :else          ^{:key (str (random-uuid))}[:div (str tree)]))]) 

(defn state-viewer []
 (let [state (subscribe [:db/get []])] 
  [:div 
   ;[state-viewer--recursion (dissoc @state :all-guilds)]
   (str (mapv first (:overlapping-areas @state)))]))