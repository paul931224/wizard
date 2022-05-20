(ns wizard.editor.sidebar.events
  (:require  [re-frame.core :refer [dispatch reg-event-fx reg-event-db subscribe]]))


(reg-event-db 
  :sidebar/component-editor
  (fn [db [_ path]]
   (-> db 
    (assoc-in [:sidebar :value] path)
    (assoc-in [:sidebar :type] :component-editor))))


(reg-event-db 
 :sidebar/component-lister
 (fn [db [_]]
  (-> db 
   (assoc-in [:sidebar :value]  nil)
   (assoc-in [:sidebar :type]   :component-lister))))
   
   
