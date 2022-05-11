(ns wizard.guild-xyz
  (:require
     [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]]
     [ajax.core :refer [GET POST]]))
 

;Common reframe utils 

(reg-event-db
 :db/set
 (fn [db [_ path value]]
   (assoc-in db path value)))

(reg-sub
 :db/get
 (fn [db [_ path]]
   (println path "oi")
   (get-in db path)))

(reg-event-fx
 :guild/get-all-guilds 
 (fn [cofx _]
   {:side-effect 
    (GET "https://api.guild.xyz/v1/guild" 
     {:handler        (fn [response] (dispatch [:db/set [:all-guilds] (js->clj response)]))
      :error-handler  (fn [response] (.log js/console "This error: " response " should be handled."))})}))
                 