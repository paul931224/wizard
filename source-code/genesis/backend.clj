(ns genesis.backend
  (:require
   [shadow.cljs.devtools.server :as server]
   [shadow.cljs.devtools.api    :as shadow]

   [x.boot-loader.api :as boot-loader]
   [genesis.routes]
   [site-backend.api]
   [gniazdo.core :as ws])

  (:gen-class))


(defn start-targeted-server!
  [{:keys  [port] :as    server-props}]
  (boot-loader/start-server! server-props)
  (println "project-emulator - Server started on port:" port))

(defn start-server!
  []
  (boot-loader/start-server!)
  (println "project-emulator - Server started"))

;; ----------------------------------------------------------------------------
;; ----------------------------------------------------------------------------


(defn -main
  [& [port :as args]]
  (if (some? port)
      (start-targeted-server! {:port  port})
      (start-server!)))    

(defn dev
  [{:keys  [port  shadow-build]}]
  (if (some? port)
      (-main port)
      (-main))
  (server/stop!)
  (server/start!)
  (shadow/watch shadow-build)
  (println "project-emulator - Development mode started"))
