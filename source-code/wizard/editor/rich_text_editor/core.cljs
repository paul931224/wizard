(ns wizard.editor.rich-text-editor.core
  (:require
   [reagent.core :refer [create-class atom]]
   [wizard.editor.rich-text-editor.events]
   [jodit-react :default JoditEditor]
   [re-frame.core :refer [dispatch subscribe]]))

   
;; -- Editor ------------------------------------------------------------------
;; ----------------------------------------------------------------------------
(defn remove-most-whitespace [content]
  (-> content
      (clojure.string/replace #"\n" "")
      (clojure.string/replace #"\t" "")))


(def jodit-settings {:language "en"
                     :minHeight "400"
                     :cleanHTML true
                     :cleanWhitespace true})

(defn jodit [value-path editor-content]
  [:> JoditEditor
   {:config    jodit-settings
    :value     @editor-content
    :tabIndex  1
    :onBlur    (fn [new-html]
                 (.log js/console (str (remove-most-whitespace new-html)))
                 (dispatch
                  [:db/set value-path (remove-most-whitespace new-html)]))}])


(defn view [{:keys [value-path]}]
  (let [local-editor-content              (atom "")
        editor-content                    (subscribe [:db/get value-path])]
    (create-class
     {:component-did-mount #(let [original  @editor-content
                                  local     @local-editor-content]
                              (reset! local-editor-content (or original "")))
      :component-did-update #(let [original  @editor-content
                                   local     @local-editor-content]
                               (reset! local-editor-content (or original "")))
      :reagent-render
      (fn [{:keys [value-path]}]
        [jodit value-path local-editor-content])})))
