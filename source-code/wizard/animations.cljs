(ns wizard.animations
  (:require
   [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]] 
   ["gsap" :as gsap])) 





(defn open-modal! []
 (let [modal         (.getElementById js/document "modal")
       modal-button  (.getElementById js/document "modal-button")
       timeline (gsap/TimelineMax. (clj->js {:delay "0.1"}))]    
   (-> timeline
       (.set modal (clj->js {:display :block 
                             :opacity 0}))
       (.to modal 0.4
            (clj->js {:ease       "power3.inOut"
                      :background "#DDD" 
                      :opacity 1}))
                      
       (.to modal-button 0.4 (clj->js {:ease       "power3.inOut" 
                                       :transform  "rotate(45deg)"}) 0))))

(defn close-modal! []
 (let [modal (.getElementById js/document "modal")
       modal-button  (.getElementById js/document "modal-button")
       timeline       (gsap/TimelineMax. (clj->js {:delay "0.1"}))]    
   (-> timeline
       (.to modal 0.4
            (clj->js {:ease  "power3.inOut"
                      :background "#DDD"
                      :onComplete (fn [a] 
                                   (.set timeline modal 
                                    (clj->js {:display :none})))}))
       (.to modal-button 0.4 (clj->js {:ease       "power3.inOut" 
                                       :transform  "rotate(0deg)"}) 0))))
                      

(reg-event-fx
 :animation/open-modal!
 (fn [db [_]]
   {:dispatch [:db/set [:modal] true]
    :side-effect (open-modal!)}))

(reg-event-fx
 :animation/close-modal!
 (fn [db [_]]
   {:dispatch [:db/set [:modal] nil]
    :side-effect (close-modal!)}))
   