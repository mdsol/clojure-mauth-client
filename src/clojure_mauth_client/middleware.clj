(ns clojure-mauth-client.middleware
  (:use clojure-mauth-client.validate))

(defn wrap-mauth-verification [handler]
  (fn[request]
    (let [{method :request-method
           uri :uri
           body :body
           headers :headers} request
          serialized-body (cond (nil? body) ""
                                (string? body) body
                                :else (slurp body))
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