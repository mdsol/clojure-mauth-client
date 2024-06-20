(defproject clojure-mauth-client "2.0.6"
  :description "Clojure Mauth Client"
  :url "https://github.com/mdsol/clojure-mauth-client"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.3"]
                 [xsc/pem-reader "0.1.1"]
                 [digest "1.4.10"]
                 [org.clojure/data.codec "0.1.1"]
                 [clojure-interop/java.security "1.0.5"]
                 [http-kit "2.4.0-alpha2"]
                 [clj-http "3.13.0"]
                 [org.clojure/data.json "2.5.0"]
                 [javax.xml.bind/jaxb-api "2.3.1"]]

  :deploy-repositories [["releases"
                         {:url           "https://clojars.org/repo"
                          :sign-releases false
                          :username      :env/CLOJARS_USERNAME
                          :password      :env/CLOJARS_DEPLOY_TOKEN}]]

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["vcs" "push"]]

  :aliases {"bump!" ^{:doc "Bump the project version number and push the commits to the original repository."}
                    ["do"
                     ["vcs" "assert-committed"]
                     ["change" "version" "leiningen.release/bump-version"]
                     ["vcs" "commit"]
                     ["vcs" "push"]]}

  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}}

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
