(ns wizard.utils
  (:require
   [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]]))


(reg-event-db
 :db/init
 (fn [db [_]]
   (-> db 
    (assoc :example {"id1" {:name "hello1" :type :block :position 0}
                     "id2" {:name "hello2" :type :block :position 1}
                     "id3" {:name "hello3" :type :block :position 2}})
    (assoc-in [:editor :type] :root)
    (assoc-in [:editor :name] "Root"))))

(reg-event-db
 :db/set
 (fn [db [_ path value]]
   (assoc-in db path value)))

(reg-sub
 :db/get
 (fn [db [_ path]]
   (get-in db path)))
