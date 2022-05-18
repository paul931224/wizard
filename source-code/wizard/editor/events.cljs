(ns wizard.editor.events
  (:require
   [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]]))


(reg-event-db
 :editor/add!
 (fn [db [_ component]]
   (assoc-in db [:editor :components (str (random-uuid))] component)))