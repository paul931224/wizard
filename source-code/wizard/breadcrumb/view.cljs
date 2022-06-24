(ns wizard.breadcrumb.view
  (:require [re-frame.core :refer [subscribe dispatch]]))

(defn one-breadcrumb [type path]
  (let [on-click (fn [] (dispatch [:db/set [:editor :selected :value-path] path]))]
     [:span.breadcrumb
      {:on-click on-click
       :style {:background "#CCC"
               :padding "5px 10px"
               :border-radius "5px"
               :cursor :pointer}}
      type])) 
      


(defn render-path [depth path]
  (let [you-are-here  (vec (take (inc depth) path))
        here          (subscribe [:db/get you-are-here])
        new-depth     (+ 2 depth)
        rest-path     (vec (drop new-depth path))
        rest-count    (count rest-path)
        type          (fn [] (str (:type @here)))]
     [:span {:style {:font-weight :bold}}
      [one-breadcrumb (type) you-are-here]
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