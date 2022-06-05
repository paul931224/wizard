(ns wizard.editor.breadcrumb
 (:require [re-frame.core :refer [subscribe dispatch]]))
 

(defn render-path [depth path]
 (let [you-are-here  (vec (take (inc depth) path))
       here          (subscribe [:db/get you-are-here])
       new-depth     (+ 2 depth)
       rest-path     (vec (drop new-depth path))
       rest-count    (count rest-path)
       type          (fn [] (str (:type @here)))
       on-click      (fn [](dispatch [:db/set [:editor :selected :value-path] you-are-here]))]
  [:span {:style {:font-weight :bold}} 
    [:span.breadcrumb {:on-click on-click}
     (str (type))]
    (if (> rest-count 0)
     [:span [:span " > "] [render-path  new-depth path]])]))
     ;[:span (type)])]))
    

(defn view []
 (let [path      (fn [] @(subscribe [:db/get [:editor :selected :value-path]]))]
  (fn [] 
   [:div {:style {:height "50px" 
                  :display :flex 
                  :justify-content :center 
                  :align-items :center}} 
     (if (< 0 (count (path))) 
      [render-path 0 (path)])])))