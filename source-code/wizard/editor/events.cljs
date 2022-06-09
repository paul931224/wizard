(ns wizard.editor.events
  (:require
   [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]]))


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

(reg-sub
 :editor/get-selected-component
 (fn [db [_]]
   (let [selected-path (-> db :editor :selected :value-path)] 
     (get-in db selected-path))))

(reg-sub
 :editor/get-selected-component-path
 (fn [db [_]]
   (-> db :editor :selected :value-path))) 
    
     