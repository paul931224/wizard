(ns wizard.animations
  (:require
   [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]] 
   ["gsap" :as gsap])) 

(defn show-guild-blocks! []
 (let [timeline (gsap/TimelineMax.)]
  (-> timeline 
   (.set "#guild-block-container"  (clj->js {:display :block}))
   (.from ".guild-block" (clj->js {:margin-left -100
                                   :opacity 0
                                   :stagger "0.2"}))))) 
                                   

(defn open-config-modal! []
  (let [config-modal          (.getElementById js/document "config-modal")
        timeline           (gsap/TimelineMax.)]
    (-> timeline
       (.set config-modal (clj->js {:visibility :visible
                                    :pointer-events :auto}))
       (.to config-modal 0.4
            (clj->js {:ease  "power3.inOut"
                      :opacity 1
                      :bottom "0"})))))

(defn close-config-modal! []
  (let [config-modal   (.getElementById js/document "config-modal")
        timeline       (gsap/TimelineMax.)]
    (-> timeline
       (.to config-modal 0.7
            (clj->js {:ease  "power3.inOut"
                      :opacity 0
                      :bottom "-100%"
                      :onComplete #(.set timeline config-modal (clj->js {:visibility     :hidden
                                                                         :pointer-events :none}))})))))

(defn open-rte-modal! []
  (let [rte-modal          (.getElementById js/document "rte-modal")
        timeline           (gsap/TimelineMax.)]  
     (-> timeline
        (.set rte-modal (clj->js {:visibility :visible})) 
                                  
        (.to rte-modal 0.4
             (clj->js {:ease  "power3.inOut"
                       :opacity 1
                       :bottom "0"})))))

(defn close-rte-modal! []
  (let [rte-modal          (.getElementById js/document "rte-modal")
        timeline       (gsap/TimelineMax.)]
     (-> timeline
        (.to rte-modal 0.7
             (clj->js {:ease  "power3.inOut"
                       :opacity 0
                       :bottom "-100%"
                       :onComplete #(.set timeline rte-modal (clj->js {:visibility     :hidden}))}))))) 
                                                                       
                       

(defn open-modal! []
 (let [modal         (.getElementById js/document "modal")
       modal-button  (.getElementById js/document "modal-button")
       hero-title    (.getElementById js/document "hero-title")
       timeline (gsap/TimelineMax.)]
   (-> timeline
       (.set modal (clj->js {:display :block 
                             :opacity 0}))
       (.to modal 0.4
            (clj->js {:ease       "power3.inOut"
                      :opacity 1}))
                      
       (.to modal-button 0.3 (clj->js {:ease       "power3.inOut" 
                                       :transform  "rotate(135deg)"}) 0)
       (.to hero-title 0.3   (clj->js {:ease       "power3.inOut" 
                                       :transform  "scale(30)"
                                       :opacity 0}) 0) 
       (.set hero-title (clj->js {:display :none})))
   (show-guild-blocks!)))
       

(defn close-modal! []
 (let [modal (.getElementById js/document "modal")
       modal-button  (.getElementById js/document "modal-button")
       hero-title    (.getElementById js/document "hero-title")
       timeline       (gsap/TimelineMax.)]  
   (-> timeline
       
       (.to modal 0.4
            (clj->js {:ease  "power3.inOut"
                      :opacity 0
                      :onComplete (fn [a] 
                                   (.set timeline modal 
                                    (clj->js {:display :none})))}))
       (.to modal-button 0.3 (clj->js {:ease       "power3.inOut" 
                                       :transform  "rotate(0deg)"}) 0)
       (.set hero-title (clj->js {:transform "scale(30)"
                                  :opacity 0 
                                  :display :flex}))
       (.to hero-title 0.3 (clj->js {:ease       "power3.inOut" 
                                     :opacity 1
                                     :transform "scale(1)"}) 0.4))))
       
(defn open-sidebar! []
 (let [sidebar        (.getElementById js/document "sidebar")
       timeline       (gsap/TimelineMax.) 
       sidebar-container  (.getElementById js/document "sidebar-container")]
    (-> timeline
       (.set sidebar-container (clj->js {:display :block}))
       (.to sidebar 0.4
            (clj->js {:ease  "power3.inOut"
                      :opacity 1 
                      :right "0"})))))

(defn close-sidebar! []
 (let [sidebar            (.getElementById js/document "sidebar")
       sidebar-container  (.getElementById js/document "sidebar-container")
       timeline       (gsap/TimelineMax.)]
    (-> timeline
       (.set sidebar-container (clj->js {:display :none}))
       (.to sidebar 0.4
            (clj->js {:ease  "power3.inOut"
                      :opacity 0 
                      :right "-100%"})))))
                      
  


(reg-event-fx
 :animation/show-guild-blocks!
 (fn [db [_]]
   {:side-effect (show-guild-blocks!)}))

(reg-event-fx
 :animation/open-rte-modal!
 (fn [db [_]]
   {:dispatch [:db/set [:modal] true]
    :side-effect (open-rte-modal!)}))

(reg-event-fx
 :animation/close-rte-modal!
 (fn [db [_]]
   {:dispatch [:db/set [:modal] nil]
    :side-effect (close-rte-modal!)}))

(reg-event-fx
 :animation/open-config-modal!
 (fn [db [_]]
   {:dispatch [:db/set [:modal] true]
    :side-effect (open-config-modal!)}))

(reg-event-fx
 :animation/close-config-modal!
 (fn [db [_]]
   {:dispatch [:db/set [:modal] nil]
    :side-effect (close-config-modal!)}))


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
   

(reg-event-fx
 :animation/open-sidebar!
 (fn [db [_]]
   {:dispatch [:db/set [:sidebar] true]
    :side-effect (open-sidebar!)}))

(reg-event-fx
 :animation/close-sidebar!
 (fn [db [_]]
   {:dispatch [:db/set [:sidebar] nil]
    :side-effect (close-sidebar!)}))