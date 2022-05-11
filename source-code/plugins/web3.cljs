(ns plugins.web3
  (:require [re-frame.core :as r]
            [cljs.core.async.interop :refer-macros [<p!]]
            [cljs.core.async :refer [go]]
            [config.smart-contract :as smart-contract]))
    
  
(set! *warn-on-infer* true)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; UTILS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Web 3 UTILS 

(defn eth [provider]
  (.-eth ^js provider))

; Web 3 SETUP


(defn new-contract [provider]
  (let [contract (.-Contract (eth ^js provider))]
    (new contract smart-contract/abi smart-contract/addr)))

; Web 3 STANDARD

(defn get-contract [db]
  (get-in db [:crypto :contract]))

(defn get-account [db]
  (get-in db [:crypto :account]))

(defn get-methods [db]
  (.-methods ^js (get-contract db)))

(defn convert-to-wei [number]
  (.toWei (.-utils js/Web3) (str number)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Actions with account 
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;




(r/reg-event-fx
  :web3/get-balance 
  (fn [{:keys [db]} [_ account]]
    (go 
      (let [provider (get-in db [:crypto :provider])
            eth      (eth ^js provider)
            balance  (<p! (.getBalance eth account))]
        (r/dispatch [:db/set-item! [:crypto :balance]  balance])))))   
  

(r/reg-event-fx
 :web3/login
 (fn [{:keys [db]} [_ theatre-rank]]
   (.log js/console "Web3 login")
   (go 
     (let [provider (get-in db [:crypto :provider])
           eth      (eth ^js provider)
           accounts (<p! (.requestAccounts eth))
           account  (aget accounts 0)]
        (r/dispatch [:web3/get-balance account])
        (r/dispatch [:db/set-item!     [:crypto :account]  account])))
   {}))


(r/reg-event-fx 
  :web3/get-accounts
  (fn [{:keys [db]} [_]]
    (go (let [provider (-> db :crypto :provider)
              eth      (eth ^js provider)
              accounts (<p! (.getAccounts eth))]
          (if-not 
            (empty? accounts)
            (r/dispatch [:web3/login]))))
    {}))
    

(r/reg-event-fx 
  :web3/setup
  (fn [db [_]]
    ;; (go (let [infura-provider  (new js/Web3 "wss://ropsten.infura.io/ws/v3/e5ba6df109e346f8b7781225ffb4de44")]
    ;;       (r/dispatch [:db/set-item! [:crypto :infura-provider] infura-provider])))
    (go (let [provider         (new js/Web3 (<p! (js/detectEthereumProvider)))]
          (r/dispatch [:db/set-item! [:crypto :provider]        provider])
          (r/dispatch [:web3/get-accounts])))))
         
