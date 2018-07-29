(defproject clojure-mauth-client "1.0"
  :description "Clojure Mauth Client"
  :url "https://www.mdsol.com"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [xsc/pem-reader "0.1.1"]
                 [digest "1.4.8"]
                 [org.clojure/data.codec "0.1.1"]
                 [http-kit "2.2.0"]
                 [org.clojure/data.json "0.2.6"]]
  :jvm-opts ~(concat
               [] ;other opts...
               (if (>= (-> (System/getProperty "java.version")
                           (clojure.string/split #"[.]")
                           first
                           Integer.) 9)
                 ["--add-modules" "java.xml.bind"]
                 []))
  :aot :all)
