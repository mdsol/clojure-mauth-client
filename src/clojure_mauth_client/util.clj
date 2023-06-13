(ns clojure-mauth-client.util
  (:require [clojure.string :refer [blank?]]
            [pem-reader.core :as pem])
  (:use digest)
  (:import (java.io ByteArrayInputStream)))

(defn epoch-seconds []
  (long (/ (System/currentTimeMillis) 1000)))

(defn msg->sha512 [msg]
  (-> msg
      .getBytes
      sha-512))

(defn get-uri [url]
  (-> url
      java.net.URL.
      .getPath
      (#(if (blank? %) "/" %))))

(defn read-key [key-str]
  (-> key-str
      .getBytes
      ByteArrayInputStream.
      pem/read))

(defn get-hex-encoded-digested-string [msg]
  (msg->sha512 msg))

(defn str->bytes
  "Convert string to byte array."
  ([^String s]
   (str->bytes s "UTF-8"))
  ([^String s, ^String encoding]
   (.getBytes s encoding)))

