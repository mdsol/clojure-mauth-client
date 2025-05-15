(ns com.mdsol.mauth.clojure.authenticator
  (:require
   [com.mdsol.mauth.clojure.convert :as convert]
   [com.mdsol.mauth.clojure.signer :as signer])
  (:import
   (com.mdsol.mauth
    Authenticator
    MAuthRequest
    MAuthRequest$Builder
    RequestAuthenticator)
   (com.mdsol.mauth.utils ClientPublicKeyProvider)
   (java.net URI)))

(set! *warn-on-reflection* true)

;; TODO: Provide factory for using any HTTP client. For now, callers supply
;;       their own ClientPublicKeyProvider impl.

(defn- ->string ^String [x]
  (if (ident? x)
    (name x)
    (str x)))

(defn- mauth-request ^MAuthRequest [request]
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

(defn valid?
  "Returns `true` if the Ring request map passes `authenticator`'s validation."
  [^Authenticator authenticator request]
  (.authenticate authenticator (mauth-request request)))

(def ^:private default-401
  {:status 401
   :body "MAuth authentication failed."})

(defn default-on-auth-failure
  ([_request]
   default-401)
  ([_request respond _raise]
   (respond default-401)))

(defn wrap-handler
  ([handler authenticator]
   (wrap-handler handler authenticator {}))
  ([handler authenticator {:keys [on-auth-failure]
                           :or {on-auth-failure default-on-auth-failure}}]
   (fn
     ([request]
      (if (valid? authenticator request)
        (handler request)
        (on-auth-failure request)))
     ([request respond raise]
      (if (valid? authenticator request)
        (handler request respond raise)
        (on-auth-failure request respond raise))))))
