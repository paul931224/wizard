(ns wizard.dom-utils)

(defn get-element-by-id [id]
  (try
    (js/document.getElementById id)
    (catch js/Error e nil)))

(defn get-bounding-client-rect [element]
  (try
    (.getBoundingClientRect element)
    (catch js/Error e nil)))

(defn get-rect-data [element]
  (let [bounding-rect (get-bounding-client-rect element)]
    (if bounding-rect
     {:top            (.-top     bounding-rect)
      :bottom         (.-bottom  bounding-rect)
      :width          (.-width   bounding-rect)
      :height         (.-height  bounding-rect)
      :left           (.-left    bounding-rect)
      :right          (.-right   bounding-rect)}
     nil)))