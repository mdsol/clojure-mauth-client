(ns clojure-mauth-client.middleware
  (:require [clojure.string :refer [lower-case]]
            [clojure.data.json :as json])
  (:use clojure-mauth-client.validate))

(defn- downcase-header-keys [headers]
  (reduce-kv
    (fn [m k v]
      (assoc m (lower-case (name k)) v))
    {} headers))

(defn wrap-mauth-verification [handler]
  (fn[request]
    (let [{method :request-method
           uri :uri
           body :body} request
          serialized-body (cond (nil? body) ""
                                (string? body) body
                                (map? body) (json/write-str body)
                                :else (slurp body))
          headers (-> (:headers request)
                      downcase-header-keys)
          [mauth-time mauth-auth mauth-version] (cond
                                                  (every? headers
                                                          ["mcc-time"
                                                           "mcc-authentication"])     [(get headers "mcc-time")
                                                                                       (get headers "mcc-authentication")
                                                                                       "v2"]
                                                  (every? headers
                                                          ["x-mws-time"
                                                           "x-mws-authentication"])   [(get headers "x-mws-time")
                                                                                       (get headers "x-mws-authentication")
                                                                                       "v1"]
                                                  :else                               (throw
                                                                                        (ex-info "No Mauth headers found"
                                                                                                 {:header-names (sort (keys headers))})))
          valid?  (validate! (.toUpperCase (name method)) uri serialized-body mauth-time mauth-auth mauth-version)]
      (if valid?
        (handler (-> request
                     (assoc :body serialized-body)))
        {:status 401
         :body "Unauthorized."}))))
