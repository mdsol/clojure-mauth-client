(ns clojure-mauth-client.header-test
  (:require [clojure.test :refer :all]
            [clojure-mauth-client.header :as header]
            [clojure-mauth-client.util :as util]
            [clojure-mauth-client.credentials :as credentials]))

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

(deftest header-test
  (testing "It should generate a valid mauth X-MWS header for GET"
    (let [creds (credentials/get-credentials)
          mauth-headers (header/build-mauth-headers "GET" "https://www.mdsol.com/api/v2/testing" "" (:app-uuid creds) (:private-key creds))]
      (is (= {"X-MWS-Authentication" "MWS abcd7d78-c874-47d9-a829-ccaa51ae75c9:gI/yUeSTbiOWggLvCv2IJP19GFvmlE8RoaUrIpyLE8DY/mCQd8CUPgT9xNHGNqgPGe9f4CZdiFCC79Xvp6seZAq8/CnqA1dsJW6f46scqqTs+4N1TJml6GNCT9xU4tjUyHWFWpCBQlSvpoTFsLSq2d2zas9M9q1sgwPBS/oPGEN1agCQLHZS/Ime4ub8MuXh0Q8aWodqCpVi4GPiap/KLIQEzbvhsdayxmAcs2XDjpt+CReRf3tBCzB1RucVEfBehxtDQGgvrs/UCUbkpq7gY7f2k0RkrH+IopfhYfdNpmCHW12OEQoZ74TVbh61Uo+xcD1der46+tWk0mdnlyXKow=="
              "X-MWS-Time"           "1532825948"}))))

  (testing "It should generate a valid mauth X-MWS header for response"
    (let [creds (credentials/get-credentials)
          mauth-headers (header/build-mauth-headers 200 "{\"test\":\"testing\"}" (:app-uuid creds) (:private-key creds))]
      (is (= {"X-MWS-Authentication" "MWS abcd7d78-c874-47d9-a829-ccaa51ae75c9:i0rLrgEN8Jy/M4kA1PNNckUxeGU2pL3PGCOjdhRrC6egHVTvNfJLXveVG/7wAU9H4hpLYsThyV1/LRc/OupBZmKYRDzqD6OneZVLkysehk6/OHKb8j8uJQnBSLB72ooIPPIUxJqtasHCi6cdzkBEZf3omp7qzkinhuX2Wi/t70xfC5YmeTydBoe2d+zcIDJZb6+zON4V5CwGMPlCLK6iFD8+hk9ddhszQZ+siHK8SWxrhrMGRDN9xyp3ljw+f62tlpTZi0KEHAr0M/aq49aP3Iv/XrN88Cl5Tt1nGIWslarfJrAxFNfzofO0KULqFB9nRV1NkBbrSMyjGvea7nGNBw=="
              "X-MWS-Time"           "1532825948"} mauth-headers)))))

