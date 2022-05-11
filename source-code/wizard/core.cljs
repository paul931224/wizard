(ns wizard.core
  (:require [wizard.guild-xyz :as guild-api]
            [reagent.core     :as reagent]
            [re-frame.core    :refer [dispatch subscribe]]
            [plugins.web3]))


(def color-one "#f4b41a")
(def color-two "#143d59")

(defn get-guilds-button []
 [:div {:style {:display :flex :justify-content :center :padding "10px 0px"}} 
   [:button {:style {:padding "5px 15px" :border "1px solid white" :background :transparent}
             :on-click #(dispatch [:guild/get-all-guilds])} "Get guilds"]])
 

(defn title []
  [:h1 {:style {:margin 0 :width "100%" 
                :display :flex 
                :justify-content :center 
                :padding "30px 0px"}}
                
   "Wizard"])

(defn wrapper [content]
 [:div {:style {:background color-one
                :min-height "100vh"}}
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
    

(defn all-guilds []
 (let [guilds (subscribe [:db/get [:all-guilds]])]
  [:div {:style {:display :flex :flex-wrap :wrap :justify-content :center}}
   (map (fn [guild]
         [guild-block guild])
        @guilds)]))

(defn view []
  (reagent/create-class
   {:component-did-mount (fn [e] (dispatch [:web3/setup]))
    :reagent-render 
    (fn []
       [wrapper
        [:<>
         [title]
         [get-guilds-button]
         [all-guilds]]])}))
    
