(ns wizard.core
  (:require 
    [wizard.animations]
    [wizard.guild-xyz]
    [wizard.utils]
    [wizard.web3]
    [reagent.core     :as reagent]
    [re-frame.core    :refer [dispatch subscribe]]))
    

(def color-one "#EEE")
(def color-two "#143d59")

(def button-style {:padding "5px 15px" :border "1px solid white" :background :transparent :cursor :pointer})

(defn get-all-guilds-button []
 [:div {:style {:display :flex :justify-content :center :padding "10px 0px"}} 
   [:button {:style button-style
             :on-click #(dispatch [:guild/get-all-guilds])} "Get all guilds"]])
 
(defn get-your-guilds-button []
 [:div {:style {:display :flex :justify-content :center :padding "10px 0px"}} 
  [:button {:style button-style
            :on-click #(dispatch [:guild/get-your-guilds])} "Get your guilds"]])

(defn connect-wallet-button []
 [:div {:style {:display :flex :justify-content :center :padding "10px 0px" :flex-wrap :wrap}} 
   [:button {:style button-style
             :on-click #(dispatch [:web3/login])} "Connect wallet"]
   [:div {:style {:width "100%" :text-align :center :padding "10px"}} 
     (str @(subscribe [:db/get [:crypto :account]]))]])


(defn hero-title []
  [:div {:style {:height "100%" 
                 :width "100%" 
                 :display :flex 
                 :justify-content :center 
                 :align-items :center}} 
    [:h1 {:style {:margin 0 :width "100%" 
                  :display :flex 
                  :justify-content :center 
                  :padding "30px 0px"}}    
     "wizard.xyz"]])

(defn header [label]
  [:h2 {:style {:margin 0 :width "100%" 
                :display :flex 
                :justify-content :center 
                :padding "15px 0px"}}
                
   label])

(defn sidebar-add-button []
 [:div {:style {:cursor :pointer}}
   [:img.sidebar-button  {:src "/images/plus-icon.png" 
                          :style {:width "50px"}}]])

(defn sidebar-button [guild]
 (let [the-name  (get guild "name")
       image     (get guild "imageUrl")]
     [:div [:img.sidebar-button  
            {:src image
             :style {:cursor :pointer
                     :width "50px" :border-radius "50%"}}]]))
  

(defn sidebar []
 (let [sidebar-guilds (subscribe [:db/get [:your-guilds]])] 
   (fn [] 
    [:div {:style {:width             "60px"
                   :flex-shrink       0
                   :padding-top       "10px"
                   :display           :flex
                   :justify-content   :center}}
     [:div {:style {:position :fixed}} 
       [sidebar-add-button]
       (if @sidebar-guilds 
        (map (fn [guild] ^{:key (random-uuid)}[sidebar-button guild]) 
             @sidebar-guilds))]])))
    

(defn wrapper [content]
 [:div {:style {:background color-one
                :min-height "100vh"
                :display :flex}}
   [sidebar]
   content])    
 
(defn https-link? [the-string]
 (if the-string 
   (clojure.string/starts-with? the-string "https")))
      

(defn guild-block-image [image]
  [:div
   [:img {:src (if (https-link? image) 
                 image 
                 (str "https://guild.xyz" image))
          :style {:width "100%"}
          :on-error (fn [e] (.log js/console image " not loaded"))}]])

(defn guild-block [guild]
 (let [the-name  (get guild "name")
       image     (get guild "imageUrl")]
     [:div {:style {:width "200px" :padding "10px" :margin "10px" :border (str "1px solid " color-two)
                    :border-radius "10px" 
                    :cursor :pointer}}
       [guild-block-image image]
       [:div {:style {:padding "10px" :text-align :center}} 
        the-name]]))
    

(defn your-guilds []
  (let [guilds (subscribe [:db/get [:your-guilds]])]
     [:div {:style {:display :flex :flex-wrap :wrap :justify-content :center}}
      (if @guilds [header "Your guilds"])
      (map (fn [guild]
            [guild-block guild])
           @guilds)]))

(defn all-guilds []
 (let [guilds (subscribe [:db/get [:all-guilds]])]
  [:div {:style {:display :flex :flex-wrap :wrap :justify-content :center}}
   (if @guilds [header "All guilds"])
   (map (fn [guild]
         [guild-block guild])
        @guilds)]))

(defn modal []
 [:div {:style {:background "#DDD" :width "100%" :height "100%"}}
  "oi"])

(defn view []
  (reagent/create-class
   {:component-did-mount (fn [e] (dispatch [:web3/setup]))
    :reagent-render 
    (fn []
       [wrapper
        [:div {:style {:flex-grow 1}}
         [hero-title]
         [modal]]])}))
         ;[connect-wallet-button]]])}))
         ;[get-all-guilds-button]
         ;[get-your-guilds-button]]])}))
         ;[your-guilds]
         ;[all-guilds]]])}))
    
