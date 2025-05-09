(ns clojure-mauth-client.header-v2-test
  (:require [clojure.test :refer :all]
            [clojure-mauth-client.header-v2 :as header-v2]
            [clojure-mauth-client.credentials :as credentials]
            [clojure-mauth-client.util :as util]))

(use-fixtures
  :once
  (fn [f]
    ;Note: these are NOT valid real credentials.
    (credentials/define-credentials "abcd7d78-c874-47d9-a829-ccaa51ae75c9"
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
    (with-redefs [util/epoch-seconds (fn [] 1532825948)]
      (f))))

(deftest header-v2-test
  (testing "It should generate a valid mauth MWSV2 header for POST"
    (let [creds (credentials/get-credentials)
          mauth-header-v2 (header-v2/build-mauth-headers-v2 "POST" "https://www.mdsol.com/api/v2/testing" "" (:app-uuid creds) (:private-key creds) "abc=testing&test=1234")]
      (is (= {"mcc-authentication" "MWSV2 abcd7d78-c874-47d9-a829-ccaa51ae75c9:nB0sLA13YT3n5HYeuox4iiGTPNrxS0XsgWRNIC51U3Pd0FFTBlbHVO+5URTywg5BAmC3hSruFP6S116ewjJE2lUoNdXSp9gsRt6GZ6Y/QGrd+U54TWKuk13rumiphljb4HdslAOe17ac6nqRxqF/Rd16lK/0nvTpPN4Z1USJ0L0YwJDUpkhaaTsvt6LIeIZjY6wexikZK/q4CvXQDwRmhtdaTICdAMjvbhJ6apnR3e8Ra9I9wmb713DudcBn2uerHuURMywTkqUCZMcH4cF3a8Hi6Lpm85BfzP+9RRzcUJbU5hkzIor58hrsvJv/9WVFGrt8JExnmcWKrgA/rj2X2g==;"
              "mcc-time"           "1532825948"} mauth-header-v2))))

  (testing "It should generate a valid mauth MWSV2 header for response"
    (let [creds (credentials/get-credentials)
          mauth-headers-v2 (header-v2/build-mauth-headers-v2 200 "{\"test\":\"testing\"}" (:app-uuid creds) (:private-key creds))]
      (is (= {"mcc-authentication" "MWSV2 abcd7d78-c874-47d9-a829-ccaa51ae75c9:pTk0Kfs6utta/wixDIwmycbPR5MMPKkU5ETZ+SlUlAMIx2o3sJxMc10ezfdO+z34l47yPIvyFytE+StMkJiLQMDpA9eKl2Xek3QdYL5YBZlG6j1b2lVgF0ptWqxXvQXlv7ets9K6BZ/HGEYj3/3IXzeNRNKYrDCTtGv8JSjdqqpLxSFMlpB++MkjXNlTh2AwIQua5ZTTSpcw8lgxA9PgXQPDeT88bqx2jhsfELG3IG3Sn9DVPOzprsicpVljYHzqjb1+wtNCaD3PYBrkDQZeVd9Y9jU2Hy6CdjnScFQvZlqg043GKQY4OJGvxVUsJDkQHkb7jzLjXfypcvvXHulWDA==;"
              "mcc-time"           "1532825948"} mauth-headers-v2)))))