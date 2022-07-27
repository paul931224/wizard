(ns wizard.sidebar.view
 (:require 
   [re-frame.core :refer [subscribe]]
   [wizard.rich-text-editor.core :as rte]
   [wizard.sidebar.config.core   :as config]))

(defn block-editor []
  (let [value-path (subscribe [:db/get [:editor :selected :value-path]])
        type         (fn [] @(subscribe [:db/get (concat @value-path [:type])]))
        content-path (fn [] (vec (concat @value-path [:content])))]

    (if (= (type) "block") 
     ^{:key (content-path)} [rte/view {:value-path (content-path)}])))

(defn view []
 [:div {:style {:width "400px"
                :height "100vh"
                :background "#222"
                :padding "10px"}}
       [block-editor]
       [config/view]])