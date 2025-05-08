(ns clojure-mauth-client.validate
  (:require [clojure.data.json :as json]
            [clojure.string :refer [trim]])
  (:use clojure-mauth-client.request
        clojure-mauth-client.credentials
        clojure.data.codec.base64))

(def ^{:private true} auth-uri "/mauth/v1/authentication_tickets.json")

(defn- build-auth-ticket-body
  [verb app-uuid request-url body time signature]
  {:authentication_ticket
   {:app_uuid app-uuid
    :verb verb
    :request_url request-url
    :b64encoded_body (String. (encode (.getBytes body)))
    :request_time time
    :client_signature signature}})

(defn signature-map [msg]
  (let [signature msg
        values-fn (fn [s] (rest (re-find #"\A(\S+)\s*([^:]+):([^:]+)\z" s)))]
    (if (nil? signature) {}
        (zipmap [:token :app-uuid :signature] (values-fn (trim signature))))))

(defn validate!
  ([verb uri body time signature]
   (validate! verb uri body time signature nil))
  ([verb uri body time signature mauth-version]
   (validate! verb uri body time signature mauth-version nil))
  ([verb uri body time signature mauth-version query-string]
   (let [creds            (get-credentials)
         sig              (signature-map signature)
         auth-body        (build-auth-ticket-body verb (:app-uuid sig) uri body time (:signature sig))
         updated-body     (cond-> auth-body
                            (= mauth-version "v2") (assoc-in [:authentication_ticket :token] (:token sig))
                            (and (= mauth-version "v2") query-string) (assoc-in [:authentication_ticket :query_string] query-string))
         auth-ticket-body (json/write-str updated-body)]
     (->
      (post! (:mauth-service-url creds)
             auth-uri
             auth-ticket-body
             :additional-headers {"Content-Type" "application/json"})
      :status
      (= 204)))))
