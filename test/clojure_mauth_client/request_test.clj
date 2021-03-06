(ns clojure-mauth-client.request-test
  (:require [clojure.test :refer :all]
            [clojure-mauth-client.request :refer :all]
            [clojure.data.json :as json])
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
    (with-redefs [clojure-mauth-client.header/epoch-seconds (fn [] 1532825948)
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
      (f)
      )
    ))

(def mock-get
  {:headers          {"X-MWS-Authentication" "MWS abcd7d78-c874-47d9-a829-ccaa51ae75c9:gI/yUeSTbiOWggLvCv2IJP19GFvmlE8RoaUrIpyLE8DY/mCQd8CUPgT9xNHGNqgPGe9f4CZdiFCC79Xvp6seZAq8/CnqA1dsJW6f46scqqTs+4N1TJml6GNCT9xU4tjUyHWFWpCBQlSvpoTFsLSq2d2zas9M9q1sgwPBS/oPGEN1agCQLHZS/Ime4ub8MuXh0Q8aWodqCpVi4GPiap/KLIQEzbvhsdayxmAcs2XDjpt+CReRf3tBCzB1RucVEfBehxtDQGgvrs/UCUbkpq7gY7f2k0RkrH+IopfhYfdNpmCHW12OEQoZ74TVbh61Uo+xcD1der46+tWk0mdnlyXKow=="
                      "X-MWS-Time"           "1532825948"}
   :url              "https://www.mdsol.com/api/v2/testing"
   :method           :get,
   :body             "\"\"",
   :throw-exceptions false
   :as               :auto}
  )

(def mock-get-with-qs
  {:headers          {"X-MWS-Authentication" "MWS abcd7d78-c874-47d9-a829-ccaa51ae75c9:gI/yUeSTbiOWggLvCv2IJP19GFvmlE8RoaUrIpyLE8DY/mCQd8CUPgT9xNHGNqgPGe9f4CZdiFCC79Xvp6seZAq8/CnqA1dsJW6f46scqqTs+4N1TJml6GNCT9xU4tjUyHWFWpCBQlSvpoTFsLSq2d2zas9M9q1sgwPBS/oPGEN1agCQLHZS/Ime4ub8MuXh0Q8aWodqCpVi4GPiap/KLIQEzbvhsdayxmAcs2XDjpt+CReRf3tBCzB1RucVEfBehxtDQGgvrs/UCUbkpq7gY7f2k0RkrH+IopfhYfdNpmCHW12OEQoZ74TVbh61Uo+xcD1der46+tWk0mdnlyXKow=="
                      "X-MWS-Time"           "1532825948"}
   :url              "https://www.mdsol.com/api/v2/testing?testABCD=1234"
   :method           :get,
   :body             "\"\"",
   :throw-exceptions false
   :as               :auto}
  )

(def mock-post
  {:headers          {"X-MWS-Authentication" "MWS abcd7d78-c874-47d9-a829-ccaa51ae75c9:kYZR1udBwE02ct55cSWq5MZuGsC6xgVmK6TWYQ/+2IAbhqG7jZWhP95bPxTqo1f12XUWsX/oeUAp8jhvdUcsXsjVMeBwvQNgnC/HP1TNQasC2LMGfOs76WnsfKoV5zWDh+SNqMqn4pIXce3DALG9d/FB2Uu4mIg9kgQIUnfJJDljfLMjR7aMgDINPU7ToM51TqTJh+ReG7LAVwsEzSwFfj4zZFFpx8XrWn3inx5ZUvT7YcFhW4vOZaeI48HSnj80bCf1LDtWXzU7yk9gon+AlIYBTtrPQTSqyofBGZUtZCBexnoEp6NUhk5XkwqK56jizT7PZCN094kh1eofr3hSTg=="
                      "X-MWS-Time"           "1532825948"},
   :url              "https://www.mdsol.com/api/v2/testing1"
   :method           :post
   :body             "\"{\\\"a\\\":{\\\"b\\\":123}}\""
   :throw-exceptions false
   :as               :auto}
  )

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

(def mock-payload (-> {:a {:b 123}}
                      json/write-str))

(deftest header-test
  (testing "It should make a valid GET request."
    (let [creds (get-credentials)]
      (-> (get! "https://www.mdsol.com" "/api/v2/testing")
          (= mock-get)
          is
          )
      ))

  (testing "It should make a valid GET request with a querystring."
    (let [creds (get-credentials)]
      (-> (get! "https://www.mdsol.com" "/api/v2/testing?testABCD=1234")
          (= mock-get-with-qs)
          is
          )
      ))

  (testing "It should make a valid POST request."
    (let [creds (get-credentials)]
      (-> (post! "https://www.mdsol.com" "/api/v2/testing1" mock-payload)
          (= mock-post)
          is
          )
      ))

  (testing "It should make a valid DELETE request."
    (let [creds (get-credentials)]
      (-> (delete! "https://www.mdsol.com" "/api/v2/testing2")
          (= mock-delete)
          is
          )
      ))

  (testing "It should make a valid PUT request."
    (let [creds (get-credentials)]
      (-> (put! "https://www.mdsol.com" "/api/v2/testing3" mock-payload)
          (= mock-put)
          is
          )
      ))
  )