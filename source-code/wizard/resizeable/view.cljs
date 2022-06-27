(ns wizard.resizeable.view)




(defn view []
 (let [] 
   [:div {:style {:position :relative}}
     [:div 
       {:style {:position :absolute 
                :top 0 
                :left 0
                :height "50px"
                :width "100px"
                :background :lightblue}}
       "Look at me MeeSeeks"]
     [:div {:style {:height "100px"}}]
     [:div {:style {:height "100px"}}]
     [:div {:style {:height "100px"}}]
     [:div {:style {:height "100px"
                    :width  "400px"
                    :background :beige}} 
      "Dropzone"]]))