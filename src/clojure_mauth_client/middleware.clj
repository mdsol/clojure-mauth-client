(ns clojure-mauth-client.middleware
  (:require [clojure.string :refer [lower-case]])
  (:use clojure-mauth-client.validate))

(defn- downcase-header-keys [headers]
  (reduce
    (fn[r [k v]]
      (-> r
          (merge {(-> (name k) lower-case) v}))
      ) {} headers)
  )

(defn wrap-mauth-verification [handler]
  (fn[request]
    (let [{method :request-method
           uri :uri
           body :body} request
          serialized-body (cond (nil? body) ""
                                (string? body) body
                                :else (slurp body))
          headers (-> (:headers request)
                      downcase-header-keys)
          valid? (validate! (.toUpperCase (name method)) uri serialized-body (get headers "x-mws-time") (get headers "x-mws-authentication"))]
      (if valid?
        (handler (-> request
                     (assoc :body serialized-body)))
        {:status 403
         :body "Unauthorized."}
        )
      )
    )
  )