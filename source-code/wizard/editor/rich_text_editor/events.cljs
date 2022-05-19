(ns wizard.editor.rich-text-editor.events
  (:require
   [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]]))



(reg-event-fx
 :rich-text-editor/open!
 (fn [{:keys [db]} [_ value-path]]
   {:dispatch-n [[:animation/open-rte-modal!]
                 [:db/set [:rich-text-editor :value-path] value-path]]}))

(reg-event-fx
 :rich-text-editor/close!
 (fn [{:keys [db]} [_ value-path]]
   {:dispatch-n [[:animation/close-rte-modal!]
                 [:db/set [:rich-text-editor :value-path] nil]]}))                