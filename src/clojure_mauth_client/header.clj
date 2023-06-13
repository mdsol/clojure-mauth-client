(ns clojure-mauth-client.header
  (:require [pem-reader.core :as pem]
            [clojure.data.codec.base64 :as base64]
            [clojure-mauth-client.util :as util])
  (:use digest)
  (:import (java.io ByteArrayInputStream)
           (javax.crypto Cipher KeyGenerator SecretKey)))

(defn- sign-mauth [message app-uuid private-key]
  (->> (let [private-key (pem/private-key (util/read-key private-key))
             cipher (doto (Cipher/getInstance "RSA/ECB/PKCS1Padding")
                      (.init Cipher/ENCRYPT_MODE private-key))]
         (.doFinal cipher (.getBytes message)))
       base64/encode
       String.
       (str "MWS " app-uuid ":")))

(defn- make-mws-auth-string [verb url body app-uuid time]
  (->> [verb (util/get-uri url) body app-uuid time]
       (clojure.string/join "\n"))
  )

(defn- make-mws-auth-string-for-response [status body app-uuid time]
  (->> [status body app-uuid time]
       (clojure.string/join "\n"))
  )

(defn build-mauth-headers
  ([verb url body app-uuid private-key]
    (let [x-mws-time (util/epoch-seconds)
          x-mws-authentication (make-mws-auth-string verb url body app-uuid x-mws-time)]
      {"X-MWS-Authentication" (-> x-mws-authentication
                                  util/msg->sha512
                                  (sign-mauth app-uuid private-key))
       "X-MWS-Time"           (str x-mws-time)}))
  ([status body app-uuid private-key]
   (let [x-mws-time (util/epoch-seconds)
         x-mws-authentication (make-mws-auth-string-for-response status body app-uuid x-mws-time)]
     {"X-MWS-Authentication" (-> x-mws-authentication
                                 util/msg->sha512
                                 (sign-mauth app-uuid private-key))
      "X-MWS-Time"           (str x-mws-time)})))

