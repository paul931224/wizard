(ns wizard.editor.components
  (:require  [re-frame.core :refer [dispatch subscribe]]
             [wizard.editor.config :refer [row-height col-width]]
             [wizard.editor.rich-text-editor.core :as rtf]
             [reagent-hickory.sweet :refer [html->hiccup]]))




(defn block [comp-router key-and-comp path]
  (let [the-key                        (first key-and-comp)
        comp-state                     (second key-and-comp)
        {:keys [content col row width height]}  comp-state
        content-path (vec (conj path :content))]        
    [:div {:on-click (fn [] (dispatch [:rich-text-editor/open! content-path]))
           :style {:pointer-events "auto"}}
     [:div.component
      [:<> (html->hiccup (str "<div>" content "</div>"))]]]))


(defn plain [comp-router key-and-comp path]
  (let [the-key                        (first key-and-comp)
        comp-state                     (second key-and-comp)
        {:keys [content col row width height]}  comp-state]
    [:div {:style {:position :absolute
                   :top    (str (* row row-height) "px")
                   :left   (str (* col col-width) "px")
                   :width  (str (* width  col-width) "px")
                   :height (str (* height row-height) "px")
                   :background :white
                   :pointer-events "auto"}}
     [:div.component (html->hiccup content)]]))
      ;[rtf/view {:value-path [:editor :components the-key :content]}]]))


(defn navbar [comp-router key-and-comp path]
  (let [comp-state                     (second key-and-comp)
        {:keys [height]}  comp-state]
    [:div {:style {:position  :relative
                   :width     "100%"
                   :background "rgba(0,0,0,0.3)"
                   :border-bottom-left-radius "10px"
                   :border-bottom-right-radius "10px"
                   :height    (str (* height row-height) "px")
                   :display :flex
                   :align-items :center
                   :pointer-events "auto"}}
     [:h2 {:style {:padding-left (str (* 1 col-width) "px")}}
      "Navbar"]]))


(defn grid-fractions [numbers-vec]
  (str
   (clojure.string/join "fr " numbers-vec) "fr"))

(defn component-wrapper [content]
  [:div.component-wrapper
     {:style {:cursor :pointer}}
     content])

(defn grid [comp-router comp-state path]
  (let [the-key          (first comp-state)
        value            (second comp-state)
        grid-rows        (:grid-rows     value)
        grid-columns     (:grid-columns  value)
        grid-components  (:components    value)
        grid-padding     (:grid-padding  value)
        grid-background  (:grid-background  value)]
    [:div.grid
     {:style {:display :grid
              :grid-template-columns (grid-fractions grid-columns)
              :grid-template-rows    (grid-fractions grid-rows)
              :pointer-events "auto"
              :justify-items :center
              :gap "10px"              
              :padding (str grid-padding "px")
              :background grid-background}}
     ;(str path " - "  (vec (concat path [:components (first comp-state)])))
     (map (fn [component]
            ^{:key the-key} [comp-router component
                             (vec (concat path [:components (first component)]))])
          (sort-by (fn [a] (:position (second a))) grid-components))]))



(defn component-router [comp-state path]
  (let [type (:type (second comp-state))]
    [component-wrapper
     (case type
       "block"  [block  component-router comp-state path]
       "plain"  [plain  component-router comp-state path]
       "navbar" [navbar component-router comp-state path]
       "grid"   [grid   component-router comp-state path]
       [block component-router comp-state path])
     path]))



(defn view []
  (let [components (subscribe [:db/get [:editor :components]])]
    [:div {:style {:position :absolute
                   :pointer-events :none
                   :width "100%"
                   :height "100%"}}
     [:<>
      (map
       (fn [comp-state] ^{:key (first comp-state)}
         [component-router
          comp-state
          [:editor :components (first comp-state)]])
       @components)]]))

