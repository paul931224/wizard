(ns genesis.frontend
  (:require [x.boot-loader.api :as boot-loader]
            [site-frontend.core.view :as core]))

(defn start-app!  [] (boot-loader/start-app!    #'core/app))
(defn render-app! [] (boot-loader/render-app!   #'core/app))
