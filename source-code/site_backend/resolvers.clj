(ns site-backend.resolvers
  (:require [com.wsscode.pathom3.connect.operation :as pco :refer [defresolver]]
            [pathom.api           :as pathom]
            [x.server-core.api :as a]))

(defresolver wizard-data 
             [env resolver-props]
             {:wizard/index "Wizard"}) 
              

(def HANDLERS [wizard-data])

(pathom/reg-handlers! ::handlers HANDLERS)