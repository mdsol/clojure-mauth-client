(ns com.mdsol.mauth.clojure.authenticator
  (:require
   [clojure.string :as str]
   [com.mdsol.mauth.clojure.convert :as convert]
   [com.mdsol.mauth.clojure.signer :as signer])
  (:import
   (clojure.lang ExceptionInfo)
   (com.mdsol.mauth
    Authenticator
    MAuthRequest
    MAuthRequest$Builder
    RequestAuthenticator)
   (com.mdsol.mauth.exception MAuthValidationException)
   (com.mdsol.mauth.utils ClientPublicKeyProvider)
   (java.net URI)))

(set! *warn-on-reflection* true)

;; TODO: Provide factory for using any HTTP client. For now, callers supply
;;       their own ClientPublicKeyProvider impl.

(defn- ->string ^String [x]
  (if (ident? x)
    (name x)
    (str x)))

(defn- mauth-request
  "Constructs an `MAuthRequest` object from a Ring request map."
  ^MAuthRequest [request]
  (let [{:keys [request-method uri body headers query-string]} request
        java-uri (URI. (str uri \? query-string))
        [t b] (convert/->bytes-or-input-stream body)]
    (-> (MAuthRequest$Builder/get)
        (.withHttpMethod (->string request-method))
        (.withResourcePath (.getRawPath java-uri))
        (.withQueryParameters (.getRawQuery java-uri))
        (.withMauthHeaders (-> headers
                               (update-keys ->string)
                               (update-vals ->string)))
        (cond->
         ,(= :bytes t) (.withMessagePayload b)
          (= :stream t) (.withBodyInputStream b))
        (.build))))

(defn default-authenticator
  "Returns an authenticator capable of validating MAuth signatures.
     
   Required arguments:
   - client-pk-provider: An instance of
     `com.mdsol.mauth.utils.ClientPublicKeyProvider`
   
   Optional arguments:
   - epoch-time-provider: A function which returns the current time as seconds
     since the Unix epoch. Defaults to a function which returns the system clock
     time.
   - v2-only: If truthy, MAuth v1 requests will fail validation. Defaults to
     `false`.
   
   The types for all of these arguments are flexible. Support for new types can
   be added by extending the protocols defined in
   `com.mdsol.mauth.clojure.convert`."
  ^RequestAuthenticator
  [& {:keys [client-pk-provider
             validation-timeout-seconds
             epoch-time-provider
             v2-only]
      :or {validation-timeout-seconds 10
           epoch-time-provider signer/current-epoch-time-provider
           v2-only false}}]
  (RequestAuthenticator. ^ClientPublicKeyProvider client-pk-provider
                         (long validation-timeout-seconds)
                         (convert/->epoch-time-provider epoch-time-provider)
                         (boolean v2-only)))

(defn mauth-data
  "Extracts key MAuth information from a Ring request and constructs a `MAuthRequest`."
  [request]
  (let [mauth-req (mauth-request request)]
    {:app-uuid (.getAppUUID mauth-req)
     :mauth-version (-> mauth-req
                        .getMauthVersion
                        .name
                        str/lower-case
                        keyword)
     :mauth-request-object mauth-req}))

(defn valid?
  "Returns `true` if the request passes `authenticator`'s validation."
  [^Authenticator authenticator request]
  (let [mauth-req (or (:mauth-request-object request)
                      (:mauth-request-object (mauth-data request)))]
    (.authenticate authenticator mauth-req)))

(defn default-on-auth-failure
  "Returns a static map with a 401 response."
  ([_]
   {:status 401
    :body {:message "Unauthorized."}})
  ;; TODO: Support async
  #_([_request respond _raise]
     (respond default-401)))

(defn wrap-handler
  "Middleware for handlers conforming to the Ring signature.
   
   Options:
   - on-auth-failure: A function called when authentication fails, which should
     return a response. Defaults to `default-on-auth-failure`. It receives a map
     with the following keys:
     - request: the request which failed authentication
     - handler: the handler wrapped by this middleware
     - exception: (optional) the exception thrown during validation, if any"
  ([handler authenticator]
   (wrap-handler handler authenticator {}))
  ([handler authenticator {:keys [on-auth-failure]
                           :or {on-auth-failure default-on-auth-failure}}]
   (fn
     ([request]
      (try
        (let [data (try
                     (mauth-data request)
                     (catch IllegalArgumentException e
                       (throw (ex-info "Invalid authentication headers."
                                       {:type ::auth-fail}
                                       e))))]
          (if (try
                (valid? authenticator data)
                (catch MAuthValidationException e
                  (throw (ex-info "Failed authentication."
                                  {:type ::auth-fail}
                                  e))))
            (handler (assoc request :com.mdsol.mauth/app-uuid (:app-uuid data)))
            (on-auth-failure {:request request
                              :handler handler})))
        (catch ExceptionInfo e
          (if (= ::auth-fail (:type (ex-data e)))
            (on-auth-failure {:request request
                              :handler handler
                              :exception e})
            (throw e)))))
     ;; TODO: Support async
     #_([request respond raise]
        (if (valid? authenticator request)
          (handler request respond raise)
          (on-auth-failure request respond raise))))))
