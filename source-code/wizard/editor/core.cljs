(ns wizard.editor.core
 (:require  [re-frame.core :refer [dispatch subscribe]]
            [wizard.editor.components.view :as components]
            [wizard.editor.toolbars.view :as toolbars]
            [wizard.editor.events]
            [wizard.editor.config :refer [row-height col-width]]
            [wizard.editor.breadcrumb  :as breadcrumb]
            [wizard.previews.order     :as order]
            [wizard.previews.selection :as selection]
            [wizard.previews.menu      :as menu]))


(defn page-wrapper [content]
 [:div {:style {:display :flex 
                :justify-content :center
                :margin-top "60px"}}
   [:div {:style {:max-width "1200px" 
                  :width "100%"}} 
     content]])
 
(defn view [] 
 [:div 
   ;[breadcrumb/view]              
   [page-wrapper 
     [components/view]]])
     
      
        
               
        
        