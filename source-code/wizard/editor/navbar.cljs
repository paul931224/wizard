(ns wizard.editor.navbar) 

(defn default []
 {:type "navbar"
  :name "Navbar"
  :height "100"})

(defn view [comp-router tree path]
  (let [comp-state                     (get-in tree path)
        {:keys [height]}  comp-state]
    [:div {:style {:position  :relative
                   :width     "100%"
                   :background "rgba(0,0,0,0.3)"
                   :border-bottom-left-radius "10px"
                   :border-bottom-right-radius "10px"
                   :height    (str height "px")
                   :display :flex
                   :align-items :center}}
                  
     [:h2 {:style {:padding-left "10px"}}
      "Navbar"]]))