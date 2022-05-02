(ns genesis.compiler
  (:require [hf.depstar                  :as hf]
            [shadow.cljs.devtools.server :as server]
            [shadow.cljs.devtools.api    :as shadow]
            [server-fruits.io            :as io])
  (:gen-class))

(def JS-CORE-PATH "resources/public/js/core")

(defn compile-app!
  [{:keys  [java-config js-builds]}]
  (io/empty-directory! JS-CORE-PATH)
  (doseq [js-build js-builds]
    (println "Compiling:" js-build)
    (shadow/release js-build))
  (hf/jar java-config))

