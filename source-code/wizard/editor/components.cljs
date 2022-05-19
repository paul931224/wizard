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
      (html->hiccup content)]]))


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

(defn component-menu-button [image-src]
  [:div.component-menu-button
   [:img {:src image-src
          :style {:height "25px" :width "25px"}}]])


(defn component-menu [path]
  (let [path-depth (dec (quot (dec (count path)) 2))]
    [:div.component-menu {:style {:right 0
                                  :top (* path-depth 30)}}
     [component-menu-button "/images/reorder-icon.png"]
     [component-menu-button "/images/resize-icon.png"]
     [component-menu-button "/images/remove-icon.png"]]))


(defn component-wrapper [content path]
  (let [path-depth (dec (quot (dec (count path)) 2))]
    [:div.component-wrapper
     {:style {:width "100%" :position :relative}}
     [component-menu path]
     content]))

(defn grid [comp-router comp-state path]
  (let [grid-rows    (:grid-rows     (second comp-state))
        grid-columns (:grid-columns  (second comp-state))
        grid-components (:components (second comp-state))]
    [:div.grid
     {:style {:display :grid
              :grid-template-columns (grid-fractions grid-columns)
              :grid-template-rows    (grid-fractions grid-rows)
              :pointer-events "auto"
              :gap "10px"}}
     ;(str path " - "  (vec (concat path [:components (first comp-state)])))
     (map (fn [component]
            ^{:key (first component)} [comp-router component
                                         (vec (concat path [:components (first component)]))])
          grid-components)]))



(defn component-router [comp-state path]
  (let [type (:type (second comp-state))]
    [component-wrapper
     (case type
       :block  [block  component-router comp-state path]
       :plain  [plain  component-router comp-state path]
       :navbar [navbar component-router comp-state path]
       :grid   [grid   component-router comp-state path]
       [plain component-router comp-state path])
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

