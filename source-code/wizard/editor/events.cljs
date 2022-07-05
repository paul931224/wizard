(ns wizard.editor.events
  (:require
   [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]]
   [wizard.utils :as utils]))


(reg-event-db
 :editor/modify!
 (fn [db [_ new-component]]
   (assoc-in db [:editor :components (str (random-uuid))] new-component)))

(reg-event-db
 :editor/remove!
 (fn [db [_ path]]
   (let [path-to-key (vec (butlast path))
         the-key     (last path)] 
    (.log js/console (str path))
    (update-in db path-to-key dissoc the-key))))

(reg-event-db
 :editor/add! 
 (fn [db [_ component]]
   (let [next-comp-count (count (-> db :editor :components))] 
    (assoc-in db [:editor :components (str (random-uuid))] 
     (assoc component :position next-comp-count)))))


(reg-event-db
 :editor/select-component! 
 (fn [db [_ path]]
   (-> db (assoc-in [:editor :selected :value-path] path))))



(defn insert-into-vec [coll insert-coll position]
 (let [[before after] (split-at position coll)]
   (vec (concat before insert-coll after))))

(defn insert-component [direction components component position]
 (let [comp-vec (utils/id-map->ordered-vector components)
       new-vec  (case direction 
                 :before (insert-into-vec comp-vec component position)
                 :after  (insert-into-vec comp-vec component (inc position))
                 components)]
      (utils/ordered-vector->id-map new-vec)))                
 

(reg-event-db
 :editor/change-type!
 (fn [db [_ path value]]
   (-> db
       (assoc-in (vec (conj path :type)) value))))

(reg-event-db
 :editor/unselect-component!
 (fn [db [_ path]]
   (-> db
         (assoc-in [:editor :selected :value-path] [:editor]))))
         

(reg-event-db
 :editor/remove-selected-component!
 (fn [db [_ path]]
   (let [components-path   (vec (butlast path))
         components        (get-in db components-path)
         new-components    (dissoc components (last path))
         new-order         (insert-component :before
                                             new-components
                                             []
                                             0)
         new-selected      (vec (conj components-path (first (last new-components))))]                  
     (-> db
      (assoc-in [:editor :selected :value-path] new-selected)
      (assoc-in components-path new-order)))))


(reg-event-db
 :editor/add-to-selected-component!
 (fn [db [_ path new-component]]
   (let [components-path   (conj path :components)
         components        (get-in db components-path)
         position          (count components)
         new-order         (insert-component :before
                                             components
                                             [(assoc new-component :id (str (random-uuid)))]
                                             position)]
     (assoc-in db  components-path new-order))))

(reg-event-db
 :editor/add-before-selected-component!
 (fn [db [_ path position new-component]]
   (let [components-path   (vec (butlast path))         
         components        (get-in db components-path)
         new-order         (insert-component :before 
                            components
                            [(assoc new-component :id (str (random-uuid)))] 
                            position)]                      
    (assoc-in db  components-path new-order))))

(reg-event-db
 :editor/add-after-selected-component!
 (fn [db [_ path position new-component]]
   (let [components-path   (vec (butlast path))
         components        (get-in db components-path)
         new-order         (insert-component :after 
                            components 
                            [(assoc new-component :id (str (random-uuid)))]
                            position)]
     (assoc-in db  components-path new-order))))

(reg-event-db
 :editor/set-overlay!
 (fn [db [_ overlay]]
   (let [old-overlay (-> db :overlays :type)]
     (if (= overlay old-overlay)
        (assoc-in db [:overlays :type] nil)
        (assoc-in db [:overlays :type] overlay)))))

(reg-sub
 :editor/get-selected-component
 (fn [db [_]]
   (let [selected-path (-> db :editor :selected :value-path)] 
     (get-in db selected-path))))

(reg-sub
 :editor/get-selected-component-path
 (fn [db [_]]
   (-> db :editor :selected :value-path))) 

(defn rem-col-from-areas [areas]
  (let [rem-last-from-vector (fn [area] (vec (butlast area)))]
     (mapv rem-last-from-vector areas)))

(defn add-col-to-areas [areas]
 (let [add-point-to-vector (fn [area] (vec (concat area ["."])))]
    (mapv add-point-to-vector areas)))

(defn rem-row-from-areas [areas]
  (vec (butlast areas)))

(defn add-row-to-areas [areas]
  (let [col-count (count (first areas))
        new-row   (mapv (fn [a] ".") (range col-count))]
     (vec (concat areas [new-row]))))

(reg-event-db
 :grid/rem-col!
 (fn [db [_]]
   (let [path       (-> db :editor :selected :value-path)
         cols-path  (vec (concat path [:cols]))
         cols       (get-in db cols-path)
         last-index (max 1 (dec (count cols)))
         areas-path (vec (concat path [:areas]))
         areas      (get-in db areas-path)]         
     (-> db 
      (assoc-in cols-path  (dissoc cols last-index))
      (assoc-in areas-path (rem-col-from-areas areas))))))

(reg-event-db
 :grid/add-col!
 (fn [db [_]]
   (let [path       (-> db :editor :selected :value-path)
         cols-path  (vec (concat path [:cols]))        
         cols       (get-in db cols-path)
         next-path  (concat path [:cols (count cols)])
         areas-path (vec (concat path [:areas]))
         areas      (get-in db areas-path)]            
     (-> db
      (assoc-in next-path "1fr")
      (assoc-in areas-path (add-col-to-areas areas))))))

(reg-event-db
 :grid/rem-row!
 (fn [db [_]]
   (let [path       (-> db :editor :selected :value-path)
         rows-path  (vec (concat path [:rows]))
         rows       (get-in db rows-path)
         last-index (max 1 (dec (count rows)))
         areas-path (vec (concat path [:areas]))
         areas      (get-in db areas-path)]
     (-> db
      (assoc-in rows-path    (dissoc rows last-index))
      (assoc-in areas-path   (rem-row-from-areas areas))))))

(reg-event-db
 :grid/add-row!
 (fn [db [_]]
   (let [path       (-> db :editor :selected :value-path)
         rows-path  (vec (concat path [:rows]))
         rows       (get-in db rows-path)
         next-path  (concat path [:rows (count rows)])
         areas-path (vec (concat path [:areas]))
         areas      (get-in db areas-path)]
     (-> db 
      (assoc-in next-path "100px")
      (assoc-in areas-path (add-row-to-areas areas)))))) 
     