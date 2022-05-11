(ns genesis.backend
  (:require
   [shadow.cljs.devtools.server :as server]
   [shadow.cljs.devtools.api    :as shadow]
   [index.core :refer [app]]
   [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))


(defn -main
  [& [port :as args]]
  (run-jetty #'app {:port 3000 :join? false})
  (println "project-emulator - Server started"))  


(defn dev
  [{:keys  [port  shadow-build]}]
  (-main)
  (server/stop!)
  (server/start!)
  (shadow/watch shadow-build)
  (println "project-emulator - Development mode started"))
