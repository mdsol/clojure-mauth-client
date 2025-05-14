(ns com.mdsol.mauth.clojure.convert
  (:require
   [clojure.string :as str])
  (:import
   (clojure.lang IFn Keyword)
   (com.mdsol.mauth MAuthVersion)
   (com.mdsol.mauth.util EpochTimeProvider)
   (java.io
    ByteArrayInputStream
    CharArrayReader
    InputStream
    StringReader)
   (java.util UUID)))

(set! *warn-on-reflection* true)

(defprotocol UUIDLike
  (as-uuid [this]))

(extend-protocol UUIDLike
  UUID
  (as-uuid [this] this)
  String
  (as-uuid [this] (parse-uuid this))
  Object
  (as-uuid [this] (as-uuid (str this))))

(defn ->uuid
  "Converts argument to a `UUID`.
   To support additional types, extend the `UUIDLike` protocol."
  ^UUID [this]
  (as-uuid this))

(defprotocol VersionLike
  (as-version [this]))

(extend-protocol VersionLike
  MAuthVersion
  (as-version [this] this)
  String
  (as-version [this] (MAuthVersion/valueOf (str/upper-case this)))
  Keyword
  (as-version [this] (as-version (name this)))
  Object
  (as-version [this] (as-version (str this))))

(defn ->version
  "Converts argument to an `MAuthVersion`.
   To support additional types, extend the `VersionLike` protocol."
  ^MAuthVersion [this]
  (as-version this))

(defprotocol EpochTimeProviderLike
  (as-epoch-time-provider ^EpochTimeProvider [this]))

(extend-protocol EpochTimeProviderLike
  EpochTimeProvider
  (as-epoch-time-provider [this] this)
  IFn
  (as-epoch-time-provider [this]
    (reify EpochTimeProvider
      (inSeconds [_]
        (long (this))))))

(defn ->epoch-time-provider
  "Converts argument to an `EpochTimeProvider`.
   To support additional types, extend the `EpochTimeProviderLike` protocol."
  ^EpochTimeProvider [this]
  (as-epoch-time-provider this))

;; The JVM does not have union types, so this is the best we can do
(defprotocol SerialData
  (as-bytes-or-input-stream [this]))

(extend-protocol SerialData
  byte/1
  (as-bytes-or-input-stream [this] [:bytes this])
  Byte/1
  (as-bytes-or-input-stream [this] [:bytes (byte-array this)])
  String
  (as-bytes-or-input-stream [this] [:bytes (.getBytes this "utf-8")])
  CharSequence
  (as-bytes-or-input-stream [this] (as-bytes-or-input-stream (str this)))
  ByteArrayInputStream
  (as-bytes-or-input-stream [this] [:bytes (.readAllBytes this)])
  StringReader
  (as-bytes-or-input-stream [this] (as-bytes-or-input-stream (slurp this)))
  CharArrayReader
  (as-bytes-or-input-stream [this] (as-bytes-or-input-stream (slurp this)))
  nil
  (as-bytes-or-input-stream [this] [:bytes this])
  InputStream
  (as-bytes-or-input-stream [this] [:stream this]))

(defn ->bytes-or-input-stream
  "Converts argument to either `byte[]` or `InputStream`.
   The return value is a vector whose first element is either `:bytes` or
   `:stream`, and whose second element is a value of the corresponding type.
   To support additional types, extend the `SerialData` protocol."
  [this]
  (as-bytes-or-input-stream this))
