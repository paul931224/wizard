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
   (assoc-in db [:editor :components (str (random-uuid))] component)))