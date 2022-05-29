(ns wizard.editor.toolbars.config.box-model)



(defn margin-box []
 [:div#margin-box {:style {:height "100px"
                           :width  "100px"
                           :background "orange"
                           :position :absolute
                           :left 0
                           :right 0
                           :margin-left :auto
                           :margin-right :auto 
                           :z-index 1}}])


(defn padding-box []
 [:div#padding-box {:style {:height "50px"
                            :width  "50px"
                            :background "lightgreen"
                            :position :absolute
                            :left 0
                            :right 0
                            :margin-left :auto
                            :margin-right :auto 
                            :z-index 3}}])


(defn border-box []
 [:div#border-box {:style {:height "80px"
                           :width "80px"
                           :background "brown"
                           :position :absolute
                           :left 0 
                           :right 0
                           :margin-left :auto
                           :margin-right :auto
                           :z-index 2}}])
               

(defn box-modal-container []
 [:div#box-model-container {:style {:width "200px"
                                    :height "200px"
                                    :position :relative
                                    :display :flex 
                                    :justify-content :center 
                                    :align-items :center}}
     [margin-box]
     [border-box]
     [padding-box]])  

(defn view []
 [box-modal-container])