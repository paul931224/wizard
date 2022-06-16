(ns wizard.previews.grid
   (:require [re-frame.core :refer [subscribe dispatch]]))

(defn view []
 (let [editor (subscribe [:db/get [:editor]])]
  (fn [] 
   [:div "grid"])))