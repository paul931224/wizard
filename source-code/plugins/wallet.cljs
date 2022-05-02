(ns plugins.wallet 
  (:require 
    [goog.string.format] 
    [goog.string :as gstring])) 

(defn convert-balance [number]
  (let [display-number      (.fromWei (.-utils js/Web3) number)]
    (gstring/format "%.3f"  display-number))) 
