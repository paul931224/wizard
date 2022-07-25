(ns wizard.utils
 (:require  [re-frame.core :refer [dispatch subscribe]]
            [reagent.core :as r]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; Data  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; Data  ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; Colors  ;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; Colors  ;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; Mouse/Touch listeners  ;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def mouse-events ["mousedown" "mouseup" "mousemove"
                   "mouseout" "mouseleave" "mouseenter"])

(def touch-events ["touchstart" "touchmove" "touchend" "touchcancel"])

(defn mouse-position [event-handler]
 (fn [event] 
  (event-handler 
   {:x (.-clientX event)
    :y (.-clientY event)})))

(defn touch-position [event-handler]
  (fn [event] 
   (let [touches (or (.-touches event) (.-changedTouches event))
         touch (get touches 0)]         
     (event-handler 
      {:x (.-clientX touch)
       :y (.-clientY touch)}))))

(def touch-fn-atom (atom nil))

(def mouse-fn-atom (atom nil))

(defn remove-touch-listeners [listener-names event-handler]
  (doseq [listener-name listener-names]
     (.removeEventListener js/document listener-name @touch-fn-atom)))

(defn remove-mouse-listeners [listener-names event-handler]
  (doseq [listener-name listener-names]
    (.removeEventListener js/document listener-name @mouse-fn-atom)))

(defn add-touch-listeners [listener-names event-handler]
  (reset! touch-fn-atom (touch-position event-handler))
  (doseq [listener-name listener-names]
   (.addEventListener js/document listener-name @touch-fn-atom)))

(defn add-mouse-listeners [listener-names event-handler]
  (reset! mouse-fn-atom (mouse-position event-handler))
  (doseq [listener-name listener-names]
     (.addEventListener js/document listener-name @mouse-fn-atom)))

(defn add-pointer-listeners [event-handler]
 (add-touch-listeners touch-events event-handler)
 (add-mouse-listeners mouse-events event-handler))
  
(defn remove-pointer-listeners [event-handler]
  (remove-touch-listeners touch-events event-handler)
  (remove-mouse-listeners mouse-events event-handler))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; Mouse/Touch listeners  ;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; State viewer ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn state-viewer--vector-display [recur-fn tree]
 [:div (str tree)])
 
(defn state-viewer--map-display-item [recur-fn the-key sub-tree]
 (let [open? (r/atom true)] 
   (fn [recur-fn the-key sub-tree] 
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
  [:div])) 
   ;[state-viewer--recursion (dissoc @state :all-guilds)]]))
   ;(str (mapv first (:overlapping-areas @state)))]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;; State viewer ;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;