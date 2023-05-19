(ns clojure-mauth-client.request
  (:require [org.httpkit.client :as http]
            [clj-http.client :as client]
            [clojure.data.json :as json])
  (:use clojure-mauth-client.header
        clojure-mauth-client.credentials)
  (:import (javax.net.ssl SSLEngine SNIHostName SSLParameters)
           (java.net URI)))

(defn- sni-configure
  [^SSLEngine ssl-engine ^URI uri]
  (let [^SSLParameters ssl-params (.getSSLParameters ssl-engine)]
    (.setServerNames ssl-params [(SNIHostName. (.getHost uri))])
    (.setSSLParameters ssl-engine ssl-params)))

(defn build-header [mauth-version query-string params]
      (let [params-with-query-string (conj params query-string)]
           (if (not (seq mauth-version))
             (apply build-mauth-headers params)
             (if (^String .equalsIgnoreCase mauth-version "v2" )
               (apply build-mauth-headers-v2 params-with-query-string)
               (apply build-mauth-headers params)))))


(defn make-request [type base-url uri body & {:keys [additional-headers with-sni? throw-exceptions?]
                                               :or {additional-headers {}
                                                    with-sni? nil
                                                    throw-exceptions? false}}]
  (let [cred (get-credentials)
        mauth-version (additional-headers :mauth-version)
        query-params (additional-headers :query-param-string)
        ; Tech debt: test with-sni?=true and modify this code if needed
        ; (https://jira.mdsol.com/browse/MCC-767309)
        options (if with-sni? {:client (http/make-client {:ssl-configurer sni-configure})} {})
        response (-> [(.toUpperCase type) (str base-url uri) (str body) (:app-uuid cred) (:private-key cred)]
                     (#(build-header mauth-version query-params %))
                     (merge additional-headers)
                     ; We use clj-http instead of http-kit because it is supported by the motel-java tracing agent
                     (#(client/request (-> {:headers %
                                          :url (str base-url uri)
                                          :method (keyword (.toLowerCase type))
                                          :body body
                                          :throw-exceptions throw-exceptions?
                                          :as :auto}
                                         (merge options)))))]
    ; The following line is here because existing clients expect a String instead of a LazySeq.
    ; When we're ready to make a breaking change, we should return "response" directly with no modification.
    (update response :body json/write-str)))

(defn get! [base-url uri & {:keys [additional-headers with-sni?]
                            :or {additional-headers {}
                                 with-sni? nil}}]
  (make-request "GET" base-url uri "" :additional-headers additional-headers
                                      :with-sni? with-sni?))

(defn post! [base-url uri body & {:keys [additional-headers with-sni?]
                                  :or {additional-headers {}
                                       with-sni? nil}}]
  (make-request "POST" base-url uri body :additional-headers additional-headers
                                         :with-sni? with-sni?))

(defn delete! [base-url uri & {:keys [additional-headers with-sni?]
                               :or {additional-headers {}
                                    with-sni? nil}}]
  (make-request "DELETE" base-url uri "" :additional-headers additional-headers
                                         :with-sni? with-sni?))

(defn put! [base-url uri body & {:keys [additional-headers with-sni?]
                                 :or {additional-headers {}
                                      with-sni? nil}}]
  (make-request "PUT" base-url uri body :additional-headers additional-headers
                                        :with-sni? with-sni?))