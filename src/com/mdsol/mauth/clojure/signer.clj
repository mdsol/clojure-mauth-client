(ns com.mdsol.mauth.clojure.signer
  (:require [clojure.string :as str])
  (:import (clojure.lang IFn Keyword)
           (com.mdsol.mauth DefaultSigner
                            MAuthVersion
                            Signer)
           (com.mdsol.mauth.util CurrentEpochTimeProvider EpochTimeProvider)
           (java.io ByteArrayInputStream
                    CharArrayReader
                    InputStream
                    StringReader)
           (java.util List UUID)))

(set! *warn-on-reflection* true)

(defmulti ->uuid-impl
  "Multimethod to convert an arbitrary type to `UUID`.
   This multimethod underlies the `->uuid` function, which simply adds the
   appropriate return type hint."
  type)
(defmethod ->uuid-impl UUID [x] x)
(defmethod ->uuid-impl String [x] (parse-uuid x))
(defmethod ->uuid-impl :default [x] (->uuid-impl (str x)))

(defn ->uuid
  "Converts an arbitrary type to `UUID`.
   To extend support to additional types, define a new method for
   `->uuid-impl`."
  ^UUID [x]
  (->uuid-impl x))

(defmulti ->version-impl
  "Multimethod to convert an arbitrary type to `MAuthVersion`.
   This multimethod underlies the `->version` function, which simply adds the
   appropriate return type hint."
  type)
(defmethod ->version-impl MAuthVersion [x] x)
(defmethod ->version-impl String [x] (MAuthVersion/valueOf (str/upper-case x)))
(defmethod ->version-impl :default [x] (->version-impl (str x)))
(defmethod ->version-impl Keyword [x] (->version-impl (name x)))

(defn ->version
  "Converts an arbitrary type to `MAuthVersion`.
   To extend support to additional types, define a new method for
   `->version-impl`."
  ^MAuthVersion [x]
  (->version-impl x))

(defmulti ->epoch-time-provider-impl
  "Multimethod to convert an arbitrary type to `EpochTimeProvider`.
   This multimethod underlies the `->epoch-time-provider` function, which simply
   adds the appropriate return type hint."
  type)
(defmethod ->epoch-time-provider-impl EpochTimeProvider [x] x)
(defmethod ->epoch-time-provider-impl IFn [x]
  (reify EpochTimeProvider
    (inSeconds [_this]
      (long (x)))))

(defn ->epoch-time-provider
  "Converts an arbitrary type to `EpochTimeProvider`.
   To extend support to additional types, define a new method for
   `->epoch-time-provider-impl`."
  ^EpochTimeProvider [x]
  (->epoch-time-provider-impl x))

(def current-epoch-time-provider
  "Provides the actual current time according to the system clock."
  (CurrentEpochTimeProvider.))

(defn default-signer
  "Returns a signer capable of producing MAuth signatures.
   
   Required arguments:
   - app-uuid: The UUID registered in the MAuth server for the signing
     application.
   - private-key: The application's private key as a String.
   Optional arguments:
   - epoch-time-provider: A function which returns the current time as seconds
     since the Unix epoch. Defaults to a function which returns the system clock
     time.
   - sign-versions: A collection of MAuth versions for which signatures should
     be produced. Defaults to `[:mwsv2]`.
   
   The types for all of these arguments are flexible. Support for new types can
   be added by installing new methods for the multimethods defined in this
   namespace."
  [& {:keys [app-uuid private-key
             epoch-time-provider sign-versions]
      :or {epoch-time-provider current-epoch-time-provider
           sign-versions [:mwsv2]}}]
  (DefaultSigner. (->uuid app-uuid)
                  ^String private-key
                  (->epoch-time-provider epoch-time-provider)
                  ^List (list* (map ->version sign-versions))))

(comment
  (def my-signer
    (default-signer {:sign-versions [:mws :mwsv2]
                     :app-uuid (random-uuid)
                     ;; This key was generated specifically for testing
                     :private-key "-----BEGIN RSA PRIVATE KEY-----
MIIEpgIBAAKCAQEA62E+E3/I+rH9PZ9Z7GwUakZeDbBqBj52sfFyt2M4LBawPojp
DBtAHOGxhCff8yncoDa8QRKTqVwn7kiTcjsuKub40/JMbnY1ltjaRm473vhMiUZm
q1c5jFyU24dEZ7DYFpC1bTpjyCeI6dgzfcqDaO366/fpuMGURGgVOQ7Fi71FKpYc
B5cZp8ywgQjSOfpM9u0e0yLukfSvDxeDGGpV1JnD9xcrojJI2iyFe2i+LPESpp6L
YHKKHilBbK3cI7uTfiIvdsYc1SLYZQtyNXDCBbvJ/zgcSTteh8D9sioL/2uLHKH3
2VRlDn0zafulP/G1pYyT89hVE1kj6P5A0m6JMwIDAQABAoIBAQDPcALCMoLQFV6m
GTKpvlJ7moo3LDs0R4ZZqf08i2+sw04NvqEL71QgX/MPEgk3BrSOac6c1p9VyYbT
ZBi1ulwkqSuvtkEPtJPj3jb3jRysk0z4Shgfdp6cCdeSZPKvI1Y9BMkvex8G/XiX
BdfTS09mgRz7KqBLGCbv5n6Vq2QDklidfrSJY8fvjzyGVZbMLUvMlFOWsA1vZQfZ
k0sKxqwgpbyoBFKsRp9CTMW6dsCEJGNFFfjvB2Eu1J5g7NN/1E4b/i4tyaDYahW4
8SeXTnm3Jr8JmfH99FGZ5CWV1Y8Kamo+AXUFdeVvwYEJJuepVxwOwisCJYvYy9IN
oV5qtnrxAoGBAPbkfqf0cCDh5wpiIfL/KwmhBdmes3fbpHF7kfNFtmBJL5RlyqsW
sNwjwLw3LEQKSmTkufZ0ozujobgtIAdRsZvzEyymVNC+YszkVgwPgKI9we+w9dHj
HKUR+2zyxoH6/FpxxzyIwXie5i+7k3Mshml9j5nX7GPJHU+i76fkkKv5AoGBAPQQ
B1ZTCfXj9ygH3ut0iUs5yK+wF0/1QV+Qu67Cub5q+ZSNGx0aKzHxuLNsPZk7uJDg
4gYvjFdq81f0Gv3JMDkULClxQif+9kUVXt9bpQbJ6S+tCCTtm2wOh6JjdN/phgAk
+wxFsDsW+DDARi2UkgILEglDDXJhi4PPm+6TSrGLAoGBAL7TMMnj9l6T/B1cZ90H
OF6C5KClNxWm4F0OI2qiMSoOpwXN/21pZl1gDPHsuvwD8Cg3WTySPjA0cySFTEIb
9GkS4XkbPmbxIDA5NACyYrwDe8glQHpvTY6QbYJxythgA3hshI/XK6JtPoEaPAdD
HMigUcOYzo75vPv2dcGQufkRAoGBAOHK3m7fngxtnd/cAEFG7Cm7SM45FCg2Fwfp
p6kTn7Hp2AK11MrExgeLwLvFvOtB1Au88X6ViLnrSTwqqrn14nY8Ems4y+Kiv4XE
MqRjbbZtIB2qcClx5WM/wf3bE2p/6ifCDrwY0OSp6G15xLMwiy/2u/XzocIbOm50
qKc8f1LnAoGBAMEBKHFEKvpQ/00RKT1VJkpUHk3SoOVey8PpaTbNFlggz/2FcO5i
JXNpe60qgURHJigvYmseF9p7f36w2cnGMpJowHhbY7QFYosuIOQ7Am8h24dgpHtd
B8+UoQ/ICy2ahrEljIQOLSqekDRq8QaRSpIZ2MNFVRPtH85R/zmxrVvT
-----END RSA PRIVATE KEY-----"})))

;; The JVM does not have union types, so this is the best we can do
(defmulti ->array-or-input-stream
  "Converts an arbitrary type to either `byte[]` or `InputStream`.
   The return value is a vector whose first element is either `:array` or
   `:input-stream`, and whose second element is a value of the corresponding
   type."
  type)

;; Lazy reading
(defmethod ->array-or-input-stream InputStream [x]
  [:input-stream x])

;; Eager reading
(defmethod ->array-or-input-stream (type (byte-array 0)) [x]
  [:array x])
(defmethod ->array-or-input-stream String [^String x]
  (->array-or-input-stream (.getBytes x "UTF-8")))
(defmethod ->array-or-input-stream CharSequence [x]
  (->array-or-input-stream (str x)))
(defmethod ->array-or-input-stream ByteArrayInputStream
  [^ByteArrayInputStream x]
  (->array-or-input-stream (.readAllBytes x)))
(defmethod ->array-or-input-stream StringReader [^StringReader x]
  (->array-or-input-stream (slurp x)))
(defmethod ->array-or-input-stream CharArrayReader [^CharArrayReader x]
  (->array-or-input-stream (slurp x)))
(defmethod ->array-or-input-stream nil [_]
  [:array nil])

(defn gen-req-headers
  "Given a signer and a Ring request, returns a map of MAuth headers."
  [^Signer signer
   {:keys [request-method body ^String uri ^String query-string]}]
  (let [method (if (ident? request-method)
                 (name request-method)
                 ^String request-method)
        [t b] (->array-or-input-stream body)]
    (into {}
          ;; The InputStream overload can only provide signatures for one
          ;; version at a time, while the array overload requires holding the
          ;; whole request body in memory. Neither fits all use cases, so we
          ;; dispatch to one or the other depending on whether the request body
          ;; is already fully held in memory.
          (case t
            :input-stream (.generateRequestHeaders signer method uri
                                                   ^InputStream b
                                                   query-string)
            :array (.generateRequestHeaders signer method uri
                                            ^bytes b
                                            query-string)))))

(comment
  (gen-req-headers my-signer {:request-method :post
                              :uri "/foo"
                              :body "Hey hey"}))

(defn wrap-client
  "Middleware for clients conforming to the Ring/clj-http signature.
   Adds MAuth headers to requests."
  [client signer]
  (fn
    ([req]
     (client (update req :headers merge (gen-req-headers signer req))))
    ([req respond raise]
     (client (update req :headers merge (gen-req-headers signer req))
             respond raise))))

(comment
  ((wrap-client prn my-signer) {:request-method :post
                                :headers {"content-type" "whatever"}
                                :uri "/foo"
                                :body "Hey hey"}))
