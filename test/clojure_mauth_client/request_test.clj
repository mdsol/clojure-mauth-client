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
                  org.httpkit.client/request (fn [{:keys [client timeout filter worker-pool keepalive as follow-redirects max-redirects response
                                                          trace-redirects allow-unsafe-redirect-methods proxy-host proxy-port tunnel?]
                                                   :as opts
                                                   :or {client nil
                                                        timeout 60000
                                                        follow-redirects true
                                                        max-redirects 10
                                                        filter nil
                                                        worker-pool nil
                                                        response (promise)
                                                        keepalive 120000
                                                        as :auto
                                                        tunnel? false
                                                        proxy-host nil
                                                        proxy-port 3128}}
                                                  & [callback]] (future opts))]
      (f)
      )
    ))

(def mock-get
  {:headers {"X-MWS-Authentication" "MWS abcd7d78-c874-47d9-a829-ccaa51ae75c9:gI/yUeSTbiOWggLvCv2IJP19GFvmlE8RoaUrIpyLE8DY/mCQd8CUPgT9xNHGNqgPGe9f4CZdiFCC79Xvp6seZAq8/CnqA1dsJW6f46scqqTs+4N1TJml6GNCT9xU4tjUyHWFWpCBQlSvpoTFsLSq2d2zas9M9q1sgwPBS/oPGEN1agCQLHZS/Ime4ub8MuXh0Q8aWodqCpVi4GPiap/KLIQEzbvhsdayxmAcs2XDjpt+CReRf3tBCzB1RucVEfBehxtDQGgvrs/UCUbkpq7gY7f2k0RkrH+IopfhYfdNpmCHW12OEQoZ74TVbh61Uo+xcD1der46+tWk0mdnlyXKow=="
             "X-MWS-Time" 1532825948}
   :url "https://www.mdsol.com/api/v2/testing"
   :method :get,
   :body "",
   :as :auto}
  )

(def mock-post
  {:headers {"X-MWS-Authentication" "MWS abcd7d78-c874-47d9-a829-ccaa51ae75c9:nP2uSWenemfLxL5UVHhGn/0DiGtC/BO2yOsAPuZooYzGujYlVUbmWj5lQqRg3RxANHb2Y9r9WxHuyMx9DSTTWIyyFdywEmC1ZCsHAW1TWPGLMsBUCZVHzp9JsLgMG1l0V59x43rD3atRQ7J4O8y32EpvEXafOKEfG6lFcOtT6uSkHxLxIQJnzKNzoXrXjw22Dw6ZAU/pfnwO21si8EwTPx1gmAvkwCfA0qJK4XzwLxWeMp11B+OBK3FK4xznEVp6eV547NKa3k1gmA6dY/mkJ0m2ltaYOK/iuEEFP12XFDvLzCOkYdK9Zvqj71IXCCayZ9AJ/shgdwGV/y1sCXWAKQ=="
             "X-MWS-Time" 1532825948},
   :url "https://www.mdsol.com/api/v2/testing1"
   :method :post
   :body "{\"a\":{\"b\":123}}"
   :as :auto}
  )

(def mock-delete
  {:headers {"X-MWS-Authentication" "MWS abcd7d78-c874-47d9-a829-ccaa51ae75c9:IzAzkGyzDtxbMlCbbWViYen/9o54B9Ijlnp1UoSIysGr/axJWwph8KRYukS+3DhYFJIBLbS1PfWI74kRTkWJl3Vmb0XgxiRfNCqresqh687ELlhNDt66p2mu/6LaVwbDKUBsIAwkQFomVfAOy3jckWZjHRySD+VABfDf4BAf5hfjTUgil63oOnH6xII51e6M160SFRz1/HpsMU/rnReniPJs22MwiqS6dhe3oU/DAzteawxujSdFA3i6Fol6kdJQN19w+0TTdOSbccjds1Wljqu/+E1ju1rXVAgcL0GuVg4dsCwrjSPY9VWfQOttpA4aHavGWNcPMh1p1kSmqlNa1g=="
             "X-MWS-Time" 1532825948}
   :url "https://www.mdsol.com/api/v2/testing2"
   :method :delete
   :body ""
   :as :auto})

(def mock-put
  {:headers {"X-MWS-Authentication" "MWS abcd7d78-c874-47d9-a829-ccaa51ae75c9:kr/Gk+QwFuNdsTLsIJi21gXr+XDGOI4j1dbDPMn2K/VaLZVwEXHwLZNH2QvsXC44DG2rJfGqsswUANVWRXSMOblLBg8ykb3LGxOzrTZ0nj8e2pQKwxknr2rA794OTJ/+tm1stpyyA4p0SX+UxiBsEv9eRWul1pI8FK0ELm8JC+7IedeeK7br0qa6yqreoLMghFuA1aALO5pgx1khK5/a3702GQqosnAqU01zy5b8ahwNQLbO35i9EbS7cHvXzDhYaSmyx//Nf/h2w3Nux37CcpibwMvcvSxhpdKRMFrlzPU7Okoc8ewrpHEyWYNq6lJgmpRfgsx3LLZGacPcA21aGQ=="
             "X-MWS-Time" 1532825948}
   :url "https://www.mdsol.com/api/v2/testing3"
   :method :put
   :body "{\"a\":{\"b\":123}}"
   :as :auto})

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