(ns wizard.utils
  (:require
   [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]]))

(reg-event-db
 :db/set
 (fn [db [_ path value]]
   (assoc-in db path value)))

(reg-sub
 :db/get
 (fn [db [_ path]]
   (get-in db path)))