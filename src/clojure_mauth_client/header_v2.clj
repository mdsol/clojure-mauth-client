(ns clojure-mauth-client.header-v2
  (:require [pem-reader.core :as pem]
            [clojure.data.codec.base64 :as base64]
            [jdk.security.Signature :as signature]
            [clojure-mauth-client.util :as util]))

(defn- encrypt-signature-rsa [private-key-string string-to-sign]
  (let [signature-instance (signature/*get-instance "SHA512withRSA")
        private-key (pem/private-key (util/read-key private-key-string))
        byte-array-to-sign (util/str->bytes (String. string-to-sign))]
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
   (let [mcc-time (util/epoch-seconds)
         params-vec (if (instance? java.lang.String verb-or-status)
                      [verb-or-status (util/get-uri url) (util/get-hex-encoded-digested-string body) app-uuid mcc-time query-param]
                      [verb-or-status (util/get-hex-encoded-digested-string body) app-uuid mcc-time])
         all-params-string (clojure.string/join "\n" params-vec)
         authentication (generate-headers-v2 all-params-string app-uuid private-key)]
     {"mcc-authentication" authentication
      "mcc-time"           (str mcc-time)})))