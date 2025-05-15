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

(def signer
  (let [{:keys [app-uuid request-time private-key-file]} signing-config]
    (signer/default-signer :app-uuid app-uuid
                           :private-key (slurp (io/file suite-base private-key-file))
                           :epoch-time-provider (constantly request-time))))

(def ignored-test-cases
  #{;; In HTTP, foo//bar is not the same as foo/bar. This case is incorrect.
    "get-normalize-multiple-slashes"
    ;; This is invalid URL syntax. Query strings may not contain spaces.
    "get-vanilla-query-space"})

(def test-cases
  ;; TODO: v1 cases
  (->> (io/file suite-base "protocols" "MWSV2")
       .listFiles
       (remove #(ignored-test-cases (.getName ^File %)))
       (map read-case)))

(defn norm-headers [m]
  (-> m
      (update-keys str/lower-case)
      (update-vals str)
      vec))

#_(use-fixtures :once
    (fn [f]
      (binding [*mauth-server-port* (PortFinder/findFreePort)]
        (FakeMAuthServer/start *mauth-server-port*)
        (try
          (FakeMAuthServer/return200)
          (Security/addProvider (BouncyCastleProvider.))
          (f)
          (finally
            (FakeMAuthServer/stop))))))

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

;; Index-based iteration, because some requests are input streams, and they
;; cannot be used as literals for evaluation.
(doseq [i (range (count test-cases))]
  (eval
   `(deftest ~(-> test-cases (nth i) :name symbol)
      (let [{:keys ~'[request-fn headers]} (nth test-cases ~i)]
        (is (= (norm-headers ~'headers)
               (norm-headers (signer/gen-req-headers signer (~'request-fn)))))
        (is (true? (auth/valid? authenticator
                                (update (~'request-fn) :headers
                                        merge ~'headers))))))))
