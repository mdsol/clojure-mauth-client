(ns clojure-mauth-client.middleware-test
  (:require [clojure-mauth-client.middleware :as middleware]
            [clojure.test :refer [deftest testing is]]
            [clojure-mauth-client.validate :refer [validate!]]
            [clojure-mauth-client.request :refer [post!]]
            [clojure-mauth-client.credentials :refer [get-credentials]]))

(def mock-post-request-v2
  {:headers        {"mcc-authentication" "MWSV2 abcd7d78-c874-47d9-a829-ccaa51ae75c9:T0XZu8X6bUcKBW/QgX0RnUg0hfbcDfm==;"
                    "mcc-time"           "1532825948"
                    :Content-Type        "application/json"
                    :Authorization       "da2-3ubdc4ekk5dw5n2dvwjumet3fq"
                    :mauth-version       "v2"}
   :url            "https://www.mdsol.com/api/v2/testing"
   :request-method :post
   :body           "\"{\"test\":{\"request\":123}}\""})

(def mock-post-request-v1
  {:headers        {"x-mws-authentication" "MWS a5a733c5-2bae-400c-aae9-6bb5b99d4130:SpjzqMFJ0cl8Lvi72TcU1qfVP9rzRWH/Jys2g==;"
                    "x-mws-time"           "1532825948"
                    :Content-Type        "application/json"
                    :Authorization       "da2-3ubdc4ekk5dw5n2dvwjumet3fq"
                    :mauth-version       "v1"}
   :url            "https://www.mdsol.com/api/v1/testing"
   :request-method :post
   :body           "\"{\"test\":{\"request\":123}}\""})

(defn mock-handler [request]
  {:body   "\"{\"test\":{\"response\":12345}}\""
   :status 200})

(deftest test-wrap-mauth-verification
  (testing "Request should get validated successfully"
           (with-redefs [validate!   (fn [& _] (constantly true))]
             (let [request-function      (middleware/wrap-mauth-verification mock-handler)
                   {:keys [status body]} (request-function mock-post-request-v2)]
               (is (= 200 status))
               (is (= "\"{\"test\":{\"response\":12345}}\"" body)))))

  (testing "Request should get invalidated and should return Unauthorized with 401 error code with v2"
           (with-redefs [validate!   (fn [& _] false)]
             (let [request-function      (middleware/wrap-mauth-verification mock-handler)
                   {:keys [status body]} (request-function mock-post-request-v2)]
               (is (= 401 status))
               (is (= "Unauthorized." body)))))

  (testing "Request should get invalidated and should return Unauthorized with 401 error code with v1"
           (with-redefs [validate!   (fn [& _] false)]
             (let [request-function      (middleware/wrap-mauth-verification mock-handler)
                   {:keys [status body]} (request-function mock-post-request-v1)]
               (is (= 401 status))
               (is (= "Unauthorized." body)))))

  (testing "Request should get validated as per v2 version"
           (with-redefs  [post! (fn [& args]
                                  {:status 204})
                          get-credentials (constantly {:mauth-service-url "http://test.com"})]
             (let [request-function      (middleware/wrap-mauth-verification mock-handler)
                   {:keys [status body]} (request-function mock-post-request-v2)]
               (is (= 200 status)))))

  (testing "Request should get validated as per v1 version"
           (with-redefs  [post! (fn [& args]
                                  {:status 204})
                          get-credentials (constantly {:mauth-service-url "http://test.com"})]
             (let [request-function      (middleware/wrap-mauth-verification mock-handler)
                   {:keys [status body]} (request-function mock-post-request-v1)]
               (is (= 200 status))))))
