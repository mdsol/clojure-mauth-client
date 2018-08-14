(ns clojure-mauth-client.header
  (:require [pem-reader.core :as pem]
            [clojure.data.codec.base64 :as base64]
            [clojure.string :refer [blank?]])
  (:use digest)
  (:import (java.io ByteArrayInputStream)
           (javax.crypto Cipher KeyGenerator SecretKey)))

(defn- epoch-seconds []
  (long (/ (System/currentTimeMillis) 1000)))

(defn- read-key [key-str]
  (-> key-str
      .getBytes
      ByteArrayInputStream.
      pem/read)
  )

(defn- msg->sha512 [msg]
  (-> msg
      .getBytes
      sha-512)
  )

(defn- get-uri [url]
  (-> url
      java.net.URL.
      .getPath
      (#(if (blank? %) "/" %))
      )
  )

(defn- sign-mauth [message app-uuid private-key]
  (->> (let [private-key (pem/private-key (read-key private-key))
             cipher (doto (Cipher/getInstance "RSA/ECB/PKCS1Padding")
                      (.init Cipher/ENCRYPT_MODE private-key))]
         (.doFinal cipher (.getBytes message)))
       base64/encode
       String.
       (str "MWS " app-uuid ":")))

(defn- make-mws-auth-string [verb url body app-uuid time]
  (->> [verb (get-uri url) body app-uuid time]
       (clojure.string/join "\n"))
  )

(defn build-mauth-headers [verb url body app-uuid private-key]
  (let [x-mws-time (epoch-seconds)
        x-mws-authentication (make-mws-auth-string verb url body app-uuid x-mws-time)]
    {"X-MWS-Authentication" (-> x-mws-authentication
                                msg->sha512
                                (sign-mauth app-uuid private-key))
     "X-MWS-Time" x-mws-time}))

