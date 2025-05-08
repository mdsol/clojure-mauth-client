(ns clojure-mauth-client.validate-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure-mauth-client.validate :as mauth-validate :refer [validate!]]
            [clojure-mauth-client.request :refer [post!]]
            [clojure-mauth-client.credentials :refer [get-credentials]]
            [clojure.data.json :as json]))

(def ^:private request-data
  {:verb         (.toUpperCase (name :post))
   :url          "https://www.mdsol.com/api/v1/testing"
   :body         "\"{\"test\":{\"request\":123}}\""
   :time         "1532825948"
   :v1-signature "MWS a5a733c5-2bae-400c-aae9-6bb5b99d4130:SpjzqMFJ0cl8Lvi72TcU1qfVP9rzRWH/Jys2g==;"
   :v2-signature "MWSV2 a5a733c5-2bae-400c-aae9-6bb5b99d4130:T0XZu8X6bUcKBW/QgX0RnUg0hfbcDfm==;"})

(deftest test-validate-success
  (with-redefs [post!           (fn [& args]
                                  {:status 204})
                get-credentials (constantly {:mauth-service-url "http://test.com"})]

    (testing "testing validate with mauth v1 version"
      (let [{:keys [verb url body time v1-signature]} request-data]
        (is (true? (validate! verb url body time v1-signature)))))

    (testing "testing validate with mauth v2 version"
      (let [{:keys [verb url body time v2-signature]}  request-data]
        (is (true? (validate! verb url body time v2-signature "v2")))))))

(deftest test-validate-failure
  (with-redefs [post!           (fn [& args]
                                  {:status 400})
                get-credentials (constantly {:mauth-service-url "http://test.com"})]

    (testing "testing validate with mauth v1 version"
      (let [{:keys [verb url body time v1-signature]}  request-data]
        (is (false? (validate! verb url body time v1-signature)))))

    (testing "testing validate with mauth v2 version"
      (let [{:keys [verb url body time v2-signature]}  request-data]
        (is (false? (validate! verb url body time v2-signature "v2")))))))

(deftest test-auth-ticket-body
  (let [auth-ticket-atom (atom nil)]
    (with-redefs [post! (fn [_ _ auth-ticket-body & _args]
                          (reset! auth-ticket-atom auth-ticket-body))
                  get-credentials (constantly {:mauth-service-url "http://test.com"})]
      (testing "V1 protocol"
        (let [{:keys [verb url body time v1-signature]} request-data
              _ (validate! verb url body time v1-signature)
              auth-ticket (json/read-str @auth-ticket-atom :key-fn keyword)]
          (is (= verb (get-in auth-ticket [:authentication_ticket :verb])))
          (is (= url (get-in auth-ticket [:authentication_ticket :request_url])))
          (is (nil? (get-in auth-ticket [:authentication_ticket :token])))
          (is (nil? (get-in auth-ticket [:authentication_ticket :query_string])))))
      (testing "V2 protocol"
        (let [{:keys [verb url body time v2-signature]} request-data
              query-string "color=black&model=mustang"
              _ (validate! verb url body time v2-signature "v2" query-string)
              auth-ticket (json/read-str @auth-ticket-atom :key-fn keyword)]
          (is (= verb (get-in auth-ticket [:authentication_ticket :verb])))
          (is (= url (get-in auth-ticket [:authentication_ticket :request_url])))
          (is (= "MWSV2" (get-in auth-ticket [:authentication_ticket :token])))
          (is (= query-string (get-in auth-ticket [:authentication_ticket :query_string]))))))))
