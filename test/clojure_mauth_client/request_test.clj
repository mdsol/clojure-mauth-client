(ns clojure-mauth-client.request-test
  (:require [clojure.test :refer :all]
            [clojure-mauth-client.request :refer :all]
            [clojure.data.json :as json]
            [clojure-mauth-client.util :as util])
  (:use [clojure-mauth-client.credentials]))

(use-fixtures
  :once
  (fn [f]
    ;Note: these are NOT valid real credentials.
    (define-credentials "abcd7d78-c874-47d9-a829-ccaa51ae75c9"
      "-----BEGIN RSA PRIVATE KEY-----
                        MIIEowIBAAKCAQEAsaa4gcNl4jx9YF7Y/6B+v29c0KBs1vxym0p4hwjZl5GteQgR
                        uFW5wM93F2lUFiVEQoM+Ti3AQjEDWdeuOIfo66LgbNLH7B3JhbkwHti/bMsq7T66
                        Gs3cOhMcKDrTswOv8x72QzsOf1FNs7Yzsu1iwJpttNg+VCRj169hQ/YI39KSuYzQ
                        aXdjK0++EPsFtr2fU7E4cHGCDlAr8Tgt2gY8xmIuLF513jqJ2fhurja+YZriGwuO
                        qdBDLVpJOB1iz8bQ1CGMMGbkYl64jfsMHMBeueP9RyZ50I2Jrr8v05qG2dYDeQYn
                        d7BjAy2VLiWewRFQRltMOWC3nbZAla+282J/swIDAQABAoIBAAvPfrK5z9szlE5E
                        3/5WqDaH686+6517WQ8z60Fm+DhYagUC4VK0+E12PX+j9AAo6BnX6dt+tSpxYbym
                        VyHQ/04zHOJ/POVYsZ4fSrCyTj+oXik5o1vG1d5SiOuvxYVAOIFcTJj5oyQZvqW0
                        9kjt+UO+wI5mVfZ4GN8s/LVs9PgURmYiSzy9Ed43kc4p9pSLQ448f7mnv134WknT
                        l/2z5/+q0DXj2Nx/JV0OsTR5KV4b0u87aSNrRldcKnfOWK9BubP4T9yGMjPyeuZP
                        XA0UQMRlGpql8I+4rRvdHio+N2lpHsFDzd7ckmjWMjXdeU2dGhE6580YbEpuLQRb
                        SN86ylkCgYEA3d/0W2S4wtmbV4X9AhLDe4vWLP1GmMcmcZ2wwuljEK14xNdeYzyD
                        z42PZ6RsGtAtdUHe6/e+7TRpz/f938f4ygawCV7zXkF65bPm54zz9o7+CM9p4P3V
                        AntDVO5IskiE6hb5A7wR0UdRNLDhov38Ngh+iUEDJ9MuZ75GWj6UbGcCgYEAzPmE
                        om9eaDC3g2hmBwTB54bAMgskRT9sdaFFJtMHx7VU0xw9E34qfD17o0LDa9n62TDv
                        getytWnluzwyhRw6UPhzseQw6R+7wtLlLPAj9mC58l34ONXwPSME0Exp/9WAVErt
                        Z0Ng6xPmifesQ/fSUM52aCIgufyQOQ1zCx9ogtUCgYBotpOKtqSEQVMRIYlg+x4L
                        Jtnz7azt2b+JC5UqyB8a9ePzcnl3eE31HKg7j9v9Y5awql/dGdWf+Yaewjms7aG7
                        JyDZq1hMebbYxekKCvnwuVenLMyZhPKM80O5x6PDkHo6SJFJc+8sx+3JYll7JUds
                        8OFXQbmNiBt0ltZ5LOO7rQKBgB1/HrYdXrGRqSbw5BXIenrt6kSJU+vfJ6V50rC2
                        l50GnDFRE/z1H/oHAv7IgcTIdo/AugaxMi2nEpcyH3cGS+IRDt0foGY72dI8dRxV
                        ZmdzHe8h1LGhH9Q8cNnk1TAqsi/vJGDC0nShxYA/MvwI8qwMOf/cQWdiUALVy6Nj
                        HrANAoGBAIK6Lh0mZLtan60F1rUoXqME7yIGQgf/cP+RKLwE36kqF5DQx9aFwPZx
                        9aWuKH+/XjdHf/J1n/AQ1j/G/WExs3UNfrvDgYea5QDnvc2gMBDRkdBwFZHYZLIn
                        e+viqMbgmORJDP/8vbpd0yZjT25ImysJE5cSCGiqHOotDs3jdlUX
                        -----END RSA PRIVATE KEY-----"
      "https://mauth-sandbox.imedidata.net")

    (with-redefs [util/epoch-seconds (fn [] 1532825948)
                  clj-http.client/request (fn [{:keys [client timeout filter worker-pool keepalive as follow-redirects max-redirects response
                                                       trace-redirects allow-unsafe-redirect-methods proxy-host proxy-port tunnel?]
                                                :as   opts
                                                :or   {client           nil
                                                       timeout          60000
                                                       follow-redirects true
                                                       max-redirects    10
                                                       filter           nil
                                                       worker-pool      nil
                                                       response         (promise)
                                                       keepalive        120000
                                                       as               :auto
                                                       tunnel?          false
                                                       proxy-host       nil
                                                       proxy-port       3128}}
                                               & [callback]] opts)]
      (f))))

(def mock-get
  {:headers          {"X-MWS-Authentication" "MWS abcd7d78-c874-47d9-a829-ccaa51ae75c9:gI/yUeSTbiOWggLvCv2IJP19GFvmlE8RoaUrIpyLE8DY/mCQd8CUPgT9xNHGNqgPGe9f4CZdiFCC79Xvp6seZAq8/CnqA1dsJW6f46scqqTs+4N1TJml6GNCT9xU4tjUyHWFWpCBQlSvpoTFsLSq2d2zas9M9q1sgwPBS/oPGEN1agCQLHZS/Ime4ub8MuXh0Q8aWodqCpVi4GPiap/KLIQEzbvhsdayxmAcs2XDjpt+CReRf3tBCzB1RucVEfBehxtDQGgvrs/UCUbkpq7gY7f2k0RkrH+IopfhYfdNpmCHW12OEQoZ74TVbh61Uo+xcD1der46+tWk0mdnlyXKow=="
                      "X-MWS-Time"           "1532825948"}
   :url              "https://www.mdsol.com/api/v2/testing"
   :method           :get,
   :body             "\"\"",
   :throw-exceptions false
   :as               :auto})

(def mock-get-with-qs
  {:headers          {"X-MWS-Authentication" "MWS abcd7d78-c874-47d9-a829-ccaa51ae75c9:gI/yUeSTbiOWggLvCv2IJP19GFvmlE8RoaUrIpyLE8DY/mCQd8CUPgT9xNHGNqgPGe9f4CZdiFCC79Xvp6seZAq8/CnqA1dsJW6f46scqqTs+4N1TJml6GNCT9xU4tjUyHWFWpCBQlSvpoTFsLSq2d2zas9M9q1sgwPBS/oPGEN1agCQLHZS/Ime4ub8MuXh0Q8aWodqCpVi4GPiap/KLIQEzbvhsdayxmAcs2XDjpt+CReRf3tBCzB1RucVEfBehxtDQGgvrs/UCUbkpq7gY7f2k0RkrH+IopfhYfdNpmCHW12OEQoZ74TVbh61Uo+xcD1der46+tWk0mdnlyXKow=="
                      "X-MWS-Time"           "1532825948"}
   :url              "https://www.mdsol.com/api/v2/testing?testABCD=1234"
   :method           :get,
   :body             "\"\"",
   :throw-exceptions false
   :as               :auto})

(def mock-post
  {:headers          {"X-MWS-Authentication" "MWS abcd7d78-c874-47d9-a829-ccaa51ae75c9:kYZR1udBwE02ct55cSWq5MZuGsC6xgVmK6TWYQ/+2IAbhqG7jZWhP95bPxTqo1f12XUWsX/oeUAp8jhvdUcsXsjVMeBwvQNgnC/HP1TNQasC2LMGfOs76WnsfKoV5zWDh+SNqMqn4pIXce3DALG9d/FB2Uu4mIg9kgQIUnfJJDljfLMjR7aMgDINPU7ToM51TqTJh+ReG7LAVwsEzSwFfj4zZFFpx8XrWn3inx5ZUvT7YcFhW4vOZaeI48HSnj80bCf1LDtWXzU7yk9gon+AlIYBTtrPQTSqyofBGZUtZCBexnoEp6NUhk5XkwqK56jizT7PZCN094kh1eofr3hSTg=="
                      "X-MWS-Time"           "1532825948"},
   :url              "https://www.mdsol.com/api/v2/testing1"
   :method           :post
   :body             "\"{\\\"a\\\":{\\\"b\\\":123}}\""
   :throw-exceptions false
   :as               :auto})

(def mock-delete
  {:headers          {"X-MWS-Authentication" "MWS abcd7d78-c874-47d9-a829-ccaa51ae75c9:IzAzkGyzDtxbMlCbbWViYen/9o54B9Ijlnp1UoSIysGr/axJWwph8KRYukS+3DhYFJIBLbS1PfWI74kRTkWJl3Vmb0XgxiRfNCqresqh687ELlhNDt66p2mu/6LaVwbDKUBsIAwkQFomVfAOy3jckWZjHRySD+VABfDf4BAf5hfjTUgil63oOnH6xII51e6M160SFRz1/HpsMU/rnReniPJs22MwiqS6dhe3oU/DAzteawxujSdFA3i6Fol6kdJQN19w+0TTdOSbccjds1Wljqu/+E1ju1rXVAgcL0GuVg4dsCwrjSPY9VWfQOttpA4aHavGWNcPMh1p1kSmqlNa1g=="
                      "X-MWS-Time"           "1532825948"}
   :url              "https://www.mdsol.com/api/v2/testing2"
   :method           :delete
   :body             "\"\"",
   :throw-exceptions false
   :as               :auto})

(def mock-put
  {:headers          {"X-MWS-Authentication" "MWS abcd7d78-c874-47d9-a829-ccaa51ae75c9:W+hIOQKAp0aEwDthAVaMa5ysJB8ddQdJdTWNonQoDuPEBVAY7F6GUXNoAZCYcosxgbm2rfpwyfLrS7U5b77GMFpnvvUwUSCgRziZNvfhuZfWUuW9po7OkWQUXCDvd/NtJdLOu6o1bKCGHYKjdaw/8AVH876afGyPF7+Ce2vD+YFRfY+zXF0MVWiS2WfwUwLSdOXb+Csnb21XT59zDs8qBg0gUj6WagZiJ+hYTbAt1zcNCdqs/mVt5hKA7ASxB9VY7GI4QM/n0EoyC/ruUm8DYS7kkxuxKeZuNkvpexFR4IPXQax1q7EtCIgw4yekegK210uxxYoOf+EBV2wMIVpKvw=="
                      "X-MWS-Time"           "1532825948"}
   :url              "https://www.mdsol.com/api/v2/testing3"
   :method           :put
   :body             "\"{\\\"a\\\":{\\\"b\\\":123}}\""
   :throw-exceptions false
   :as               :auto})

(def mock-post-v2
  {:headers          {"mcc-authentication" "MWSV2 abcd7d78-c874-47d9-a829-ccaa51ae75c9:EHrD5j5crMQ2Gv8YrEuZJKpeh0glru9ze+A2a4PoPDVyqz5DSJYmrREqRHVhIn3PrRUwFSECk+cMOH1IMOoiEe0nRg0nRWyZkbPDXfVpnQoq9cZXCmUSGCe5q8WHfqgu+gJCuXflI7n84QAFjxo7Iq+wl8oNC/RSqyqTWXPMF3dtIjiumo5091xhXyqEsUodvOtnYImvTX75pohyHjrrSdsWuVltQmQWjTzYeP4zKNmvznXVMG30BwLLuT97r5/NiyV2h9RbAMyxkGxKzcE56q8e7gddflIQGD/dgxHedcoihCzm5yHSqRFzx/X80LhCSWPlrcCBIUPGDREkUKbFwg==;"
                      "mcc-time"           "1532825948"
                      :Content-Type        "application/json"
                      :Authorization       "da2-3ubdc4ekk5dw5n2dvwjumet3fq"
                      :mauth-version       "v2"}
   :url              "https://www.mdsol.com/api/v2/testing1"
   :method           :post
   :body             "\"{\\\"a\\\":{\\\"b\\\":123}}\""
   :throw-exceptions false
   :as               :auto})

(def mock-put-v2
  {:headers          {"mcc-authentication" "MWSV2 abcd7d78-c874-47d9-a829-ccaa51ae75c9:nBKyoJMY5qvumQhLhP0aZr2xe+058aaMuWeP8+fH3MDvxCJTectJsVPO/17wgCwP14yG/EqHK2z8/SrEjMADiOM4QtCTPgcByonCuZzcWW+zSncpssLB3ItC8K7OJ9/urZ60wOxu+0v3Nhl+jYzrVB8fIxq3HELxxIhrq1Bt41BdLNNUTBR2RG6cB2cCHB5sw2bScC1BiuwS73zOkP59Q2uRpsCfdYjWj+u9WvDV1oakUflfjaZHMqsyGMgtMMl6idkR4OejyElyQ/gv4i0dPamR4m+VogMPnlQKyGsqRyXYu1Eo8jpQR+8Mu1pa9Xyu45v18jhOJPlLpF2/1HCp2w==;"
                      "mcc-time"           "1532825948"
                      :Content-Type        "application/json"
                      :Authorization       "da2-3ubdc4ekk5dw5n2dvwjumet3fq"
                      :mauth-version       "v2"}
   :url              "https://www.mdsol.com/api/v2/testing1"
   :method           :put
   :body             "\"{\\\"a\\\":{\\\"b\\\":123}}\""
   :throw-exceptions false
   :as               :auto})

(def mock-get-v2
  {:headers          {"mcc-authentication" "MWSV2 abcd7d78-c874-47d9-a829-ccaa51ae75c9:fdWPNlo6rTdMKxw5wf0KpQMKm22Zuf6PIUQRwIYtyzZgNEgJjgjlVdQogSYHTBVuLMx1iuyXH9m/Mex0jzD0DFZh/YIBRQ2Mn1ChKE5T0ejSyLjaTGHFg6sIpCWxVZzUvPKZKTDDdtk12vovGcRWBTfY62LpJpjlcI1YMEwcCL5A7fgIwwX9pxQO/AVJUDA9cowbBpDIjJlo9ZdcdBWfLcKfgPoWtEO5UDpvZZti0rSTiRLY3a0/l8xPLpuQXq9EjUrac0srunEK6te4XQ9MuhEQRYRkTIMF4Hvqxz9HYtkByuvpiSqNfXKCGXGxYhfAFVvRUhmk2RagISwj4qzbUA==;"
                      "mcc-time"           "1532825948"
                      :Content-Type        "application/json"
                      :Authorization       "da2-3ubdc4ekk5dw5n2dvwjumet3fq"
                      :mauth-version       "v2"}
   :url              "https://www.mdsol.com/api/v2/testing1"
   :method           :get
   :body             "\"\""
   :throw-exceptions false
   :as               :auto})

(def mock-get-with-qs-v2
  {:headers          {"mcc-authentication" "MWSV2 abcd7d78-c874-47d9-a829-ccaa51ae75c9:fdWPNlo6rTdMKxw5wf0KpQMKm22Zuf6PIUQRwIYtyzZgNEgJjgjlVdQogSYHTBVuLMx1iuyXH9m/Mex0jzD0DFZh/YIBRQ2Mn1ChKE5T0ejSyLjaTGHFg6sIpCWxVZzUvPKZKTDDdtk12vovGcRWBTfY62LpJpjlcI1YMEwcCL5A7fgIwwX9pxQO/AVJUDA9cowbBpDIjJlo9ZdcdBWfLcKfgPoWtEO5UDpvZZti0rSTiRLY3a0/l8xPLpuQXq9EjUrac0srunEK6te4XQ9MuhEQRYRkTIMF4Hvqxz9HYtkByuvpiSqNfXKCGXGxYhfAFVvRUhmk2RagISwj4qzbUA==;" "mcc-time" "1532825948"
                      :Content-Type        "application/json"
                      :Authorization       "da2-3ubdc4ekk5dw5n2dvwjumet3fq"
                      :mauth-version       "v2"}
   :url              "https://www.mdsol.com/api/v2/testing1?testABCD=1234"
   :method           :get
   :body             "\"\""
   :throw-exceptions false
   :as               :auto})

(def mock-delete-v2
  {:headers          {"mcc-authentication" "MWSV2 abcd7d78-c874-47d9-a829-ccaa51ae75c9:f2qLS57HqU7ZoJx5hlhIt9nyjbB/tqiOwcWe3Y5lO7OLL2OKuR3Et8nF5SNEu4ToWrr67nu/16ztdNRC0138uRVVEdIhnPgD3z9WZZehHaoP64BHBEcleP6MBFVJ2p/9nJvqjvZ64qAkovzQf6lGQdBSp93X8MrDN/BMSxSGOUUXffPst4nzzl09dhgCCnk0vqHG8/wDELi2I5ieCxt3WVMDj+rcffm+C0e6Oc7qT4WVXYwxnVOSOZRWo7cpd5dEXTfG0KSJK47Zdoy/qlrhSY4byk+qwky57lROMixNxdeE5PNTh6qmvvvZWRVpb+vKJraP849eacgPEyBIvAezwA==;"
                      "mcc-time"           "1532825948"
                      :Content-Type        "application/json"
                      :Authorization       "da2-3ubdc4ekk5dw5n2dvwjumet3fq"
                      :mauth-version       "v2"}
   :url              "https://www.mdsol.com/api/v2/testing1"
   :method           :delete
   :body             "\"\""
   :throw-exceptions false
   :as               :auto})

(def additional-headers {:Content-Type  "application/json"
                         :Authorization "da2-3ubdc4ekk5dw5n2dvwjumet3fq"
                         :mauth-version "v2"})

(def mock-payload (-> {:a {:b 123}}
                      json/write-str))

(deftest header-test
  (get-credentials)
  (testing "It should make a valid GET request."
    (let [get-response (get! "https://www.mdsol.com" "/api/v2/testing")]
      (is (= mock-get get-response))))

  (testing "It should make a valid GET request with a querystring."
    (let [get-response (get! "https://www.mdsol.com" "/api/v2/testing?testABCD=1234")]
      (is (= mock-get-with-qs get-response))))

  (testing "It should make a valid POST request."
    (let [post-response (post! "https://www.mdsol.com" "/api/v2/testing1" mock-payload)]
      (is (= mock-post post-response))))

  (testing "It should make a valid DELETE request."
    (let [delete-response (delete! "https://www.mdsol.com" "/api/v2/testing2")]
      (is (= mock-delete delete-response))))

  (testing "It should make a valid PUT request."
    (let [put-response (put! "https://www.mdsol.com" "/api/v2/testing3" mock-payload)]
      (is (= mock-put put-response))))

  (testing "It should make a valid POST request with v2 mauth header"
    (let [post-response (post! "https://www.mdsol.com" "/api/v2/testing1" mock-payload :additional-headers additional-headers
                               :with-sni? false)]
      (is (= mock-post-v2 post-response))))

  (testing "It should make a valid PUT request with v2 mauth header"
    (let [put-response (put! "https://www.mdsol.com" "/api/v2/testing1" mock-payload :additional-headers additional-headers
                             :with-sni? false)]
      (is (= mock-put-v2 put-response))))

  (testing "It should make a valid GET request with v2 mauth header"
    (let [get-response (get! "https://www.mdsol.com" "/api/v2/testing1" :additional-headers additional-headers
                             :with-sni? false)]
      (is (= mock-get-v2 get-response))))

  (testing "It should make a valid GET request with query string and with v2 mauth header"

    (let [get-response (get! "https://www.mdsol.com" "/api/v2/testing1?testABCD=1234" :additional-headers additional-headers
                             :with-sni? false)]
      (is (= mock-get-with-qs-v2 get-response))))

  (testing "It should make a valid DELETE request with v2 mauth header"
    (let [delete-response (delete! "https://www.mdsol.com" "/api/v2/testing1" :additional-headers additional-headers
                                   :with-sni? false)]
      (is (= mock-delete-v2 delete-response)))))
