(ns clojure-mauth-client.middleware-test
  (:require [clojure-mauth-client.middleware :as middleware]
            [clojure.test :refer [deftest testing is]]
            [clojure-mauth-client.validate :refer [validate!]]))

(def mock-post-request
  {:headers        {"mcc-authentication" "MWSV2 abcd7d78-c874-47d9-a829-ccaa51ae75c9:T0XZu8X6bUcKBW/QgX0RnUg0hfbcDfm==;"
                    "mcc-time"           "1532825948"
                    :Content-Type        "application/json"
                    :Authorization       "da2-3ubdc4ekk5dw5n2dvwjumet3fq"
                    :mauth-version       "v2"}
   :url            "https://www.mdsol.com/api/v2/testing"
   :request-method :post
   :body           "\"{\"test\":{\"request\":123}}\""})

(defn mock-handler [request]
  {:body   "\"{\"test\":{\"response\":12345}}\""
   :status 200})

(deftest test-wrap-mauth-verification
  (testing "Request should get validated successfully"
           (with-redefs [validate!   (fn [& _] (constantly true))]
             (let [f                     (middleware/wrap-mauth-verification mock-handler)
                   {:keys [status body]} (f mock-post-request)]
               (is (= 200 status))
               (is (= "\"{\"test\":{\"response\":12345}}\"" body)))))

  (testing "Request should get invalidated and should return Unauthorized with 401 error code"
           (with-redefs [validate!   (fn [& _] false)]
             (let [f                     (middleware/wrap-mauth-verification mock-handler)
                   {:keys [status body]} (f mock-post-request)]
               (is (= 401 status))
               (is (= "Unauthorized." body))))))
