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

;; TODO: Provide factory for using any HTTP client. For now, callers supply
;;       their own ClientPublicKeyProvider impl.

(defn- ->string ^String [x]
  (if (ident? x)
    (name x)
    (str x)))

(defn- mauth-request ^MAuthRequest [request]
  (prn request)
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

(defn default-authenticator ^RequestAuthenticator
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

(defn valid? [^Authenticator authenticator request]
  (.authenticate authenticator (mauth-request request)))
