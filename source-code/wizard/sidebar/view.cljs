(ns wizard.sidebar.view
 (:require 
   [re-frame.core :refer [subscribe]]
   [wizard.rich-text-editor.core :as rte]
   [wizard.sidebar.config.core   :as config]
   [reagent.core :as reagent :refer [atom]]))

(defn block-editor []
  (let [value-path (subscribe [:db/get [:editor :selected :value-path]])
        type         (fn [] @(subscribe [:db/get (concat @value-path [:type])]))
        content-path (fn [] (vec (concat @value-path [:content])))]

    (if (= (type) "block") 
     ^{:key (content-path)} [rte/view {:value-path (content-path)}])))


(def scroll-atom (atom 0))

(defn scroll-handler [e]
 (reset! scroll-atom (.-scrollY js/window)))

(defn view []
 (reagent/create-class
      {:component-did-mount #(.addEventListener js/window "scroll" scroll-handler)
       :component-will-unmount #(.removeEventListener js/window "scroll" scroll-handler)
       :reagent-render 
       (fn []
        [:div {:style {:width "400px"}}
              [:div {:style {:position :absolute 
                             :top (str @scroll-atom "px")
                             :height "100vh"
                             :overflow-y "auto"
                             :background "#222"
                             :width "100%"
                             :color :white}}
               [:div {:style {:padding "10px"}}
                [block-editor]
                [config/view]]]])}))