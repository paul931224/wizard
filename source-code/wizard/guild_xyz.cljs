(ns wizard.guild-xyz
  (:require
     [re-frame.core :refer [dispatch reg-event-db reg-event-fx reg-sub]]
     [ajax.core :refer [GET POST]]))
 

(reg-event-fx
 :guild/get-all-guilds 
 (fn [cofx _]
   {:side-effect 
    (GET "https://api.guild.xyz/v1/guild" 
     {:handler        (fn [response] (dispatch [:db/set [:all-guilds] (js->clj response :keywordize-keys true)]))
      :error-handler  (fn [response] (.log js/console "This error: " response " should be handled."))})}))
                 

(reg-event-fx
 :guild/get-your-guilds 
 (fn [{:keys [db]} [_ account]]
   {:side-effect 
     (GET (str "https://api.guild.xyz/v1/guild/address/" account "?order=members") 
      {:handler        (fn [response] 
                         (dispatch [:db/set [:your-guilds] (js->clj response :keywordize-keys true)])
                         (dispatch [:animation/show-guild-blocks!]))
       :error-handler  (fn [response] (.log js/console "This error: " response " should be handled."))})}))
        