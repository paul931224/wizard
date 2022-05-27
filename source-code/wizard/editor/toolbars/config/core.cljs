(ns wizard.editor.toolbars.config.core
 (:require [re-frame.core :refer [dispatch subscribe]]))

(defn view []
  (let [selected-path (subscribe [:db/get [:editor :selected :value-path]])] 
   [:div "Selected " @selected-path]))