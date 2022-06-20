(ns wizard.core
  (:require 
    [wizard.animations]
    [wizard.guild-xyz]
    [wizard.utils]
    [wizard.web3]
    [wizard.editor.core :as editor]
    [reagent.core     :as reagent]
    [re-frame.core    :refer [dispatch subscribe]]
    [plugins.drag-and-drop :as dnd]
    [wizard.toolbars.view        :as toolbars]
    [wizard.overlays.selection   :as selection]
    [wizard.overlays.order       :as order]
    [wizard.overlays.grid        :as grid]
    [wizard.overlays.menu        :as menu]
    [wizard.editor.breadcrumb    :as breadcrumb]))
    

(def color-one "#EEE")
(def color-two "#333")

(def button-style {:padding "15px 30px" 
                   :border "0px"
                   :display :flex 
                   :justify-content :center 
                   :align-items :center
                   :background "#333"
                   :color :white
                   :cursor :pointer
                   :height "60px"
                   :font-size "24px"
                   :font-weight "500"})

(defn get-all-guilds-button []
 [:div {:style {:display :flex :justify-content :center :padding "10px 0px"}} 
   [:button {:style button-style
             :on-click #(dispatch [:guild/get-all-guilds])} "Browse all guilds"]])
 


(defn connect-wallet-button []
 [:div {:style {:display :flex 
                :justify-content :center 
                :align-items :center
                :padding "10px 0px" 
                :flex-wrap :wrap 
                :height "100%"}}
  [:button {:style button-style
            :on-click #(dispatch [:web3/login])} "Connect wallet"]])
   


(defn hero-title []
  [:div {:style {:height "100%" 
                 :width "100%" 
                 :display :flex 
                 :justify-content :center 
                 :align-items :center}} 
    [:h1#hero-title {:style {:margin 0 :width "100%" 
                             :display :flex 
                             :justify-content :center 
                             :padding "30px 0px"}}    
     "wizard.xyz"]])

(defn sidebar-add-button []
 (let [modal? (subscribe [:db/get [:modal]])] 
   [:div#modal-button {:style {:width "50px" 
                               :height "50px"}}
     [:img.sidebar-button  
       {:on-click (fn [a]
                    (if @modal? 
                      (dispatch [:animation/close-modal!])
                      (dispatch [:animation/open-modal!])))
        :src "/images/plus-icon.png" 
        :style {:height "100%"
                :width "100%"
                :cursor :pointer}}]]))

(defn sidebar-button [guild]
 (let [image     (get guild "imageUrl")]
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
    

(defn main-content-wrapper [content]
 [:div#main-content {:style {:background color-one
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
     [:div.guild-block
       {:style {:width "200px" :padding "10px" :margin "10px"
                :border-radius "10px" 
                :cursor :pointer                 
                :background :white 
                :box-shadow "5px 5px 15px -4px #000000"}}
       [guild-block-image image]
       [:div {:style {:padding "10px" :text-align :center}} 
        the-name]]))
    

(defn your-guilds []
  (let [guilds (subscribe [:db/get [:your-guilds]])]
     [:div#guild-block-container {:style {:display :none
                                          :padding "15px"}}
      (if @guilds 
        [:h2 "Your guilds"]
        [:h2 "Create some guilds first"])
      [:div {:style {:display :flex :flex-wrap :wrap}}
       (map (fn [guild]
             ^{:key (random-uuid)}[guild-block guild])
            @guilds)]]))


(defn all-guilds []
  (let [guilds (subscribe [:db/get [:all-guilds]])]
     [:div {:style {:padding "15px"}}
       (if @guilds [:h2 "All guilds"])
       [:div {:style {:display :flex :flex-wrap :wrap :justify-content :center}}
        (map (fn [guild]
              [guild-block guild])
            @guilds)]]))


(defn modal []
 (let [account (subscribe [:db/get [:crypto :account]])] 
  [:div#modal {:style {:position :absolute
                       :top 0
                       :display :none
                       :background "#CCC" :width "100%" :height "100%"}}
   (if @account
    [:<> 
     [your-guilds]]
     ;[all-guilds]]
    [connect-wallet-button])]))
    
(defn page-wrapper [content]
  [:div {:style {:display :flex
                 :justify-content :center
                 :margin-top "60px"}}
   [:div {:style {:max-width "1200px"
                  :position :relative                  
                  :width "100%"}}
    content]])


(defn with-z-index [number content]
 [:div {:style {:z-index number}}
  content])

(defn page-wrapper-with-z-index [number content]
  [:div {:style {:z-index number}}
   [page-wrapper content]])


(defn the-editor []
  [:div {:style {:position :relative}}
        [with-z-index 2 [breadcrumb/view]]
        [with-z-index 2 [menu/view]]
        [with-z-index 1 [page-wrapper  
                            [:<> 
                             [with-z-index 1 [editor/view]]
                             [grid/view]
                             [order/view]]]]
                             
        [with-z-index 3 [toolbars/view]]])

(defn view []
  (reagent/create-class
    {:component-did-mount (fn [e] 
                           (dispatch [:web3/setup])
                           (dispatch [:db/init]))                 
     :reagent-render 
     (fn [] 
       (let [guild-selected (subscribe [:db/get [:guild-selected]])] 
         [main-content-wrapper
          [:<> 
            [selection/view @(subscribe [:db/get [:editor]])]
            [:div {:style {:flex-grow 1}}
             (if @guild-selected
               [hero-title]
               [the-editor])
             (identity [modal])]]]))}))
       
         
         
         
         
    
    
