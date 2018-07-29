(ns clojure-mauth-client.credentials
  (:require [clojure.string :refer [blank?]]))

(defonce ^:private credential (atom {}))

(defn define-credentials [app-uuid private-key mauth-service-url]
  (let [cred {:app-uuid app-uuid
              :private-key private-key
              :mauth-service-url mauth-service-url}]
    (if (or (blank? app-uuid)
            (blank? private-key)
            (blank? mauth-service-url))
      (throw (Exception. "Please be sure the app-uuid, private-key and mauth-service-url are defined and valid,
                          either in the environment variables APP_UUID, MAUTH_KEY and MAUTH_SERVICE_URL respectively.
                          Or, you may call define-credentials to set them manually."))
      (reset! credential cred)
      )
    )
  )

(defn get-credentials []
  (if (empty? @credential)
    (define-credentials (System/getenv "APP_UUID")
                        (System/getenv "MAUTH_KEY")
                        (System/getenv "MAUTH_SERVICE_URL")))
  @credential
  )