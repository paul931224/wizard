(ns wizard.editor.events
  (:require
   [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]]
   [wizard.data-structures :as data-structures]))


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



(defn insert-into-vec [coll element position]
 (let [[before after] (split-at position coll)]
   (vec (concat before [element] after))))

(defn insert-component [direction components component position]
 (let [comp-vec (data-structures/id-map->ordered-vector components)
       new-vec  (case direction 
                 :before (insert-into-vec comp-vec component position)
                 :after  (insert-into-vec comp-vec component (inc position))
                 components)]
      (data-structures/ordered-vector->id-map new-vec)))                
 

(reg-event-db
 :editor/add-before-component!
 (fn [db [_ path position new-component]]
   (let [components-path   (vec (butlast path))         
         components        (get-in db components-path)
         new-order         (insert-component :before components new-component position)] 
    (assoc-in db  components-path new-order))))

(reg-event-db
 :editor/add-after-component!
 (fn [db [_ path position new-component]]
   (let [components-path   (vec (butlast path))
         components        (get-in db components-path)
         new-order         (insert-component :after components new-component position)]
     (assoc-in db  components-path new-order))))

(reg-sub
 :editor/get-selected-component
 (fn [db [_]]
   (let [selected-path (-> db :editor :selected :value-path)] 
     (get-in db selected-path))))

(reg-sub
 :editor/get-selected-component-path
 (fn [db [_]]
   (-> db :editor :selected :value-path))) 
    
     