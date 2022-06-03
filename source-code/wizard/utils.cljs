(ns wizard.utils
  (:require
   [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]]))


(reg-event-db
 :db/init
 (fn [db [_]]
   (-> db 
    (assoc-in [:toolbars :grid :type] "grid")
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
