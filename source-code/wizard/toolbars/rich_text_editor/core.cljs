(ns wizard.toolbars.rich-text-editor.core
  (:require
   [reagent.core :refer [create-class atom] :as reagent]
   [jodit-react :default JoditEditor]
   [re-frame.core :refer [dispatch subscribe]]))

    
;; -- Editor ------------------------------------------------------------------
;; ----------------------------------------------------------------------------
(defn remove-most-whitespace [content]
  (-> content
      (clojure.string/replace #"\n" "")
      (clojure.string/replace #"\t" "")))


(def jodit-settings {:language "en"
                     :minHeight "300"
                     :cleanHTML true
                     :enter "div"
                     :cleanWhitespace true})

(defn jodit [value-path editor-content]
  [:div {:style {:max-width "400px"}} 
    [:> JoditEditor
     {:config    jodit-settings
      :value     @editor-content
      :tabIndex  1
      :onChange    (fn [new-html]
                     (.log js/console (str (remove-most-whitespace new-html)))
                     (dispatch
                      [:db/set value-path (remove-most-whitespace new-html)]))}]])


(defn view [{:keys [value-path]}]
  (let [local-editor-content              (atom "")
        editor-content                    (fn [value-path] 
                                            (subscribe [:db/get value-path]))]
    (create-class
     {:component-did-mount #(let [original  @(editor-content value-path)]
                              (reset! local-editor-content (or original "")))
      :component-did-update (fn [this old-arg] 
                              (let [new-argv (rest (reagent/argv this))
                                    new-value-path (:value-path (first new-argv))
                                    original  @(editor-content new-value-path)]                              
                                (reset! local-editor-content (or original ""))))
      :reagent-render
      (fn [{:keys [value-path]}]
        ^{:key value-path}[jodit value-path local-editor-content])})))


