(defproject clojure-mauth-client "2.0.1"
  :description "Clojure Mauth Client"
  :url "https://github.com/mdsol/clojure-mauth-client"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [xsc/pem-reader "0.1.1"]
                 [digest "1.4.8"]
                 [org.clojure/data.codec "0.1.1"]
                 [clojure-interop/java.security "1.0.5"]
                 [clojure-interop/java.net "1.0.5"]
                 [org.bouncycastle/bcpkix-jdk15on "1.53"]
                 [http-kit "2.4.0-alpha2"]
                 [clj-http "3.9.1"]
                 [org.clojure/data.json "0.2.6"]
                 [javax.xml.bind/jaxb-api "2.2.11"]]
  :jvm-opts ~(concat
               [] ;other opts...
               (if (let [v (-> (System/getProperty "java.version")
                         (clojure.string/split #"[.]")
                         first
                         Integer.)]
                     (and (>= v 9) (< v 11)))
                 ["--add-modules" "java.xml.bind"]
                 []))
  :aot :all)
