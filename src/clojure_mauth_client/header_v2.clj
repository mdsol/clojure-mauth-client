(ns clojure-mauth-client.header-v2
  (:require [pem-reader.core :as pem]
            [clojure.data.codec.base64 :as base64]
            [clojure.string :refer [blank?]]
            [jdk.security.Signature :as signature])
  (:use digest)
  (:import (java.io ByteArrayInputStream)))

(defn- epoch-seconds []
  (long (/ (System/currentTimeMillis) 1000)))

(defn- msg->sha512 [msg]
  (-> msg
      .getBytes
      sha-512))

(defn- get-uri [url]
  (-> url
      java.net.URL.
      .getPath
      (#(if (blank? %) "/" %))
      )
  )

(defn- read-key [key-str]
  (-> key-str
      .getBytes
      ByteArrayInputStream.
      pem/read)
  )

(defn- get-hex-encoded-digested-string [msg]
  (msg->sha512 msg))

(defn- str->bytes
  "Convert string to byte array."
  ([^String s]
   (str->bytes s "UTF-8"))
  ([^String s, ^String encoding]
   (.getBytes s encoding)))

(defn- encrypt-signature-rsa [private-key-string string-to-sign]
  (let [signature-instance (signature/*get-instance "SHA256withRSA")
        private-key (pem/private-key (read-key private-key-string))
        byte-array-to-sign (str->bytes (String. string-to-sign))]
    (signature/init-sign signature-instance private-key)
    (signature/update signature-instance byte-array-to-sign 0 (alength byte-array-to-sign))
    (->> (signature/sign signature-instance)
         base64/encode
         String.
         (str ""))))

(defn- generate-headers-v2 [mcc-auth-string-to-sign app-uuid private-key]
  (let [encrypted-signature (encrypt-signature-rsa private-key mcc-auth-string-to-sign)
        mcc-authentication (str "MWSV2" " " app-uuid ":" encrypted-signature ";")]
    mcc-authentication))

(defn build-mauth-headers-v2
  ([verb-or-status body app-uuid private-key]
   (build-mauth-headers-v2 verb-or-status nil body app-uuid private-key nil))
  ([verb-or-status url body app-uuid private-key query-param]
   (let [mcc-time (epoch-seconds)
         params-vec (if (instance? java.lang.String verb-or-status)
                      [verb-or-status (get-uri url) (get-hex-encoded-digested-string body) app-uuid mcc-time query-param]
                      [verb-or-status (get-hex-encoded-digested-string body) app-uuid mcc-time])
         all-params-string (clojure.string/join "\n" params-vec)
         authentication (generate-headers-v2 all-params-string app-uuid private-key)]
     {"mcc-authentication" authentication
      "mcc-time"           (str mcc-time)})))