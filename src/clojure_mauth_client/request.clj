(ns clojure-mauth-client.request
  (:require [org.httpkit.client :as http])
  (:use clojure-mauth-client.header
        clojure-mauth-client.credentials))

(defn make-request [type base-url uri body & {:keys [additional-headers]
                                               :or {additional-headers {}}}]
  (let [cred (get-credentials)
        response (-> [(.toUpperCase type) (str base-url uri) (str body) (:app-uuid cred) (:private-key cred)]
                     (#(apply build-mauth-headers %))
                     (merge additional-headers)
                     (#(http/request {:headers %
                                      :url (str base-url uri)
                                      :method (keyword (.toLowerCase type))
                                      :body body
                                      :as :auto})))]
    @response
    )
  )

(defn get! [base-url uri & {:keys [additional-headers]
                            :or {additional-headers {}}}]
  (make-request "GET" base-url uri "" :additional-headers additional-headers)
  )

(defn post! [base-url uri body & {:keys [additional-headers]
                                  :or {additional-headers {}}}]
  (make-request "POST" base-url uri body :additional-headers additional-headers)
  )

(defn delete! [base-url uri & {:keys [additional-headers]
                               :or {additional-headers {}}}]
  (make-request "DELETE" base-url uri "" :additional-headers additional-headers)
  )

(defn put! [base-url uri body & {:keys [additional-headers]
                                 :or {additional-headers {}}}]
  (make-request "PUT" base-url uri body :additional-headers additional-headers)
  )