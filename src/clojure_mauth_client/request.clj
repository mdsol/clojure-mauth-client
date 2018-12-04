(ns clojure-mauth-client.request
  (:require [org.httpkit.client :as http])
  (:use clojure-mauth-client.header
        clojure-mauth-client.credentials)
  (:import (javax.net.ssl SSLEngine SNIHostName SSLParameters)
           (java.net URI)))

(defn- sni-configure
  [^SSLEngine ssl-engine ^URI uri]
  (let [^SSLParameters ssl-params (.getSSLParameters ssl-engine)]
    (.setServerNames ssl-params [(SNIHostName. (.getHost uri))])
    (.setSSLParameters ssl-engine ssl-params)))



(defn make-request [type base-url uri body & {:keys [additional-headers with-sni?]
                                               :or {additional-headers {}
                                                    with-sni? nil}}]
  (let [cred (get-credentials)
        options (if with-sni? {:client (http/make-client {:ssl-configurer sni-configure})} {})
        response (-> [(.toUpperCase type) (str base-url uri) (str body) (:app-uuid cred) (:private-key cred)]
                     (#(apply build-mauth-headers %))
                     (merge additional-headers)
                     (#(http/request (-> {:headers %
                                          :url (str base-url uri)
                                          :method (keyword (.toLowerCase type))
                                          :body body
                                          :as :auto}
                                         (merge options)))))]
    @response
    )
  )

(defn get! [base-url uri & {:keys [additional-headers with-sni?]
                            :or {additional-headers {}
                                 with-sni? nil}}]
  (make-request "GET" base-url uri "" :additional-headers additional-headers
                                      :with-sni? with-sni?)
  )

(defn post! [base-url uri body & {:keys [additional-headers with-sni?]
                                  :or {additional-headers {}
                                       with-sni? nil}}]
  (make-request "POST" base-url uri body :additional-headers additional-headers
                                         :with-sni? with-sni?)
  )

(defn delete! [base-url uri & {:keys [additional-headers with-sni?]
                               :or {additional-headers {}
                                    with-sni? nil}}]
  (make-request "DELETE" base-url uri "" :additional-headers additional-headers
                                         :with-sni? with-sni?)
  )

(defn put! [base-url uri body & {:keys [additional-headers with-sni?]
                                 :or {additional-headers {}
                                      with-sni? nil}}]
  (make-request "PUT" base-url uri body :additional-headers additional-headers
                                        :with-sni? with-sni?)
  )