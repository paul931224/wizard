(ns config.smart-contract
  #?(:clj   (:require 
              [cheshire.core     :as cheshire])))              

(def addr "insert contract address here")

(def abi-str "[{\"insert\": \"data here\"}]")

(def abi 
  #?(:clj   (cheshire/parse-string abi-str)
     :cljs  (.parse js/JSON abi-str)))
