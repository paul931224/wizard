(ns wizard.previews.overlay 
  (:require [re-frame.core :refer [subscribe dispatch]]
            [wizard.previews.menu      :as menu]
            [wizard.previews.selection :as selection]))
                                 
               
(defn view []
   [:<> 
    [menu/view]
    [selection/view @(subscribe [:db/get [:editor]])]])                  

                    
                             