(ns com.mdsol.mauth.clojure.signer-authenticator-test
  (:require
   [camel-snake-kebab.core :as csk]
   [charred.api :as charred]
   [clojure.java.io :as io]
   [clojure.string :as str]
   [clojure.test :refer [deftest is]]
   [com.mdsol.mauth.clojure.authenticator :as auth]
   [com.mdsol.mauth.clojure.signer :as signer])
  (:import
   (com.mdsol.mauth.exception MAuthValidationException)
   (com.mdsol.mauth.util MAuthKeysHelper)
   (com.mdsol.mauth.utils ClientPublicKeyProvider)
   (java.io File FilenameFilter)
   (java.net URI)))

(set! *warn-on-reflection* true)

(def suite-base (io/file "mauth-protocol-test-suite"))

(defn child-by-ext [^File parent ext]
  (first (.listFiles parent (reify FilenameFilter
                              (accept [_this _dir file-name]
                                (str/ends-with? file-name ext))))))

(defn ringify-request [{:keys [verb url body body-filepath]} path]
  ;; Don't reinvent the wheel, just let Java parse the URI.
  ;; It's a more complex task than you think.
  (let [java-uri (URI. url)]
    {:request-method verb
     :uri (.getRawPath java-uri)
     :query-string (.getRawQuery java-uri)
     :body (or body
               (when body-filepath
                 (io/input-stream (io/file path body-filepath))))}))

(defn read-case [^File path]
  {:name (.getName path)
   ;; Recreate request each time because some contain stateful streams
   :request-fn #(some-> (child-by-ext path ".req")
                        (charred/read-json :key-fn csk/->kebab-case-keyword)
                        (ringify-request path))
   ;; This implementation does not expose these intermediate steps in a testable
   ;; way. That's fine, because they aren't part of the public contract anyway
   ;; and don't really need to be tested directly.
   #_#_:str-to-sign (some-> (child-by-ext path ".sts")
                            slurp)
   #_#_:signature (some-> (child-by-ext path ".sig")
                          slurp)
   :headers (some-> (child-by-ext path ".authz")
                    charred/read-json)})

(def signing-config
  (-> (io/file suite-base "signing-config.json")
      (charred/read-json :key-fn csk/->kebab-case-keyword)
      (update :app-uuid parse-uuid)))

(def signer-v2
  (let [{:keys [app-uuid request-time private-key-file]} signing-config]
    (signer/default-signer :app-uuid app-uuid
                           :private-key (slurp (io/file suite-base private-key-file))
                           :epoch-time-provider (constantly request-time))))

(def signer-v1
  (let [{:keys [app-uuid request-time private-key-file]} signing-config]
    (signer/default-signer :app-uuid app-uuid
                           :private-key (slurp (io/file suite-base private-key-file))
                           :epoch-time-provider (constantly request-time)
                           :sign-versions [:mws])))

(def ignored-test-cases
  #{;; In HTTP, foo//bar is not the same as foo/bar. This case is incorrect.
    "get-normalize-multiple-slashes"
    ;; This is invalid URL syntax. Query strings may not contain spaces.
    "get-vanilla-query-space"})

(def test-cases-v2
  (->> (.listFiles (io/file suite-base "protocols" "MWSV2"))
       (remove #(ignored-test-cases (.getName ^File %)))
       (map read-case)))

(def test-cases-v1
  (->> (.listFiles (io/file suite-base "protocols" "MWS"))
       (remove #(ignored-test-cases (.getName ^File %)))
       (map read-case)))

(defn norm-headers [m]
  (-> m
      (update-keys str/lower-case)
      (update-vals str)
      vec
      (->> (sort-by first))))

(def pub-key
  (MAuthKeysHelper/getPublicKeyFromString
   (slurp (io/file suite-base "signing-params" "rsa-key-pub")
          :encoding "utf-8")))

(def pk-provider
  (reify ClientPublicKeyProvider
    (getPublicKey [_ app-uuid]
      (if (= app-uuid (:app-uuid signing-config))
        pub-key
        (throw (ex-info "Unexpected key requested"
                        {:received app-uuid
                         :expected (:app-uuid signing-config)}))))))

(def authenticator
  (auth/default-authenticator :client-pk-provider pk-provider
                              :epoch-time-provider (constantly 1444672125)))

(defn validate-test-case [test-case signer]
  (let [{:keys [request-fn headers]} test-case]
    (is (= (norm-headers headers)
           (norm-headers (signer/gen-req-headers signer (request-fn))))
        "Signer produces expected headers")
    ;; = only compares true on streams if it's the same instance
    (let [req (request-fn)]
      (is (= (-> req
                 (update :headers merge headers)
                 (update :headers norm-headers))
             (update ((signer/wrap-client identity signer)
                      req)
                     :headers norm-headers))
          "Client middleware adds expected headers"))
    (is (true? (auth/valid? authenticator
                            (update (request-fn) :headers
                                    merge headers)))
        "Authenticator validates headers")
    (let [req (update (request-fn) :headers
                      merge headers)]
      (is (= req
             ((auth/wrap-handler identity authenticator)
              (update req :headers
                      merge headers)))
          "Server middleware passes through on success"))
    (is (= {:status 401
            :body "oops!"
            ::got-request true}
           ((auth/wrap-handler identity authenticator
                               {:on-auth-failure
                                (fn [{:keys [request exception handler]}]
                                  (is (nil? exception))
                                  (is (= identity handler))
                                  {:status 401
                                   :body "oops!"
                                   ::got-request (::is-request request)})})
            (-> (request-fn)
                (update :headers merge headers)
                (assoc :body "This is not the right body!")
                (assoc ::is-request true))))
        "Server middleware calls on-auth-failure on failure")
    (is (= {:status 401
            :body "oops!"
            ::got-request true}
           ((auth/wrap-handler identity authenticator
                               {:on-auth-failure
                                (fn [{:keys [request exception handler]}]
                                  (is (instance? MAuthValidationException 
                                                 exception))
                                  (is (= identity handler))
                                  {:status 401
                                   :body "oops!"
                                   ::got-request (::is-request request)})})
            (-> (request-fn)
                (update :headers merge headers)
                (assoc-in [:headers "X-MWS-Time"] 1)
                (assoc-in [:headers "MCC-Time"] 1)
                (assoc ::is-request true))))
        "Server middleware calls on-auth-failure on exception")
    (is (= {:status 401
            :body {:message "MAuth request validation failed because request time was older than10s"}}
           ((auth/wrap-handler identity authenticator)
            (-> (request-fn)
                (update :headers merge headers)
                (assoc-in [:headers "X-MWS-Time"] 1)
                (assoc-in [:headers "MCC-Time"] 1))))
        "default-on-auth-failure returns exception message on exception")
    (is (= {:status 401
            :body {:message "MAuth authentication failed."}}
           ((auth/wrap-handler identity authenticator)
            (-> (request-fn)
                (update :headers merge headers)
                (assoc :body "This is not the right body!"))))
        "default-on-auth-failure returns default message on auth failure")))

(doseq [i (range (count test-cases-v2))]
  (eval
   `(deftest ~(-> test-cases-v2
                  (nth i)
                  :name
                  (->> (str "mwsv2-"))
                  symbol)
      (validate-test-case (nth test-cases-v2 ~i) signer-v2))))

(doseq [i (range (count test-cases-v1))]
  (eval
   `(deftest ~(-> test-cases-v1
                  (nth i)
                  :name
                  (->> (str "mws-"))
                  symbol)
      (validate-test-case (nth test-cases-v1 ~i) signer-v1))))
