(ns ui.components
  (:require
   [x.app-core.api     :as a])) 

(defn is-mobile? [number]
  (let [viewport-width  (a/subscribe [:db/get-item [:environment/viewport-data :meta-items :viewport-width]])]
    (< @viewport-width number)))

(defn scroll-to [id]
  (.scrollIntoView (.getElementById js/document id)
                   (clj->js {:behavior    "smooth" 
                             :alignToTop  true})))
