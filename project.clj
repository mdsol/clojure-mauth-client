(defproject clojure-mauth-client "3.0.0-SNAPSHOT"
  :description "Clojure Mauth Client"
  :url "https://github.com/mdsol/clojure-mauth-client"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[camel-snake-kebab "0.4.3"]
                 [com.cnuernber/charred "1.033"]
                 [com.mdsol/mauth-test-utils "16.0.0+0-a6fb9a5f+20240725-1833-SNAPSHOT"]
                 [org.clojure/clojure "1.12.0"]
                 #_[xsc/pem-reader "0.1.1"]
                 #_[digest "1.4.10"]
                 #_[org.clojure/data.codec "0.1.1"]
                 #_[clojure-interop/java.security "1.0.5"]
                 #_[org.clojure/data.json "2.5.0"]
                 #_[javax.xml.bind/jaxb-api "2.3.1"]
                 [com.mdsol/mauth-signer "16.0.0"]]

  :repositories [["maven-prod-virtual" {:url      "https://mdsol.jfrog.io/mdsol/maven-prod-virtual"
                                        :username :env/artifactory_username
                                        :password :env/artifactory_password}]]

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

  :jvm-opts ~(concat
              [] ;other opts...
              (if (let [v (-> (System/getProperty "java.version")
                              (clojure.string/split #"[.]")
                              first
                              Integer.)]
                    (and (>= v 9) (< v 11)))
                ["--add-modules" "java.xml.bind"]
                [])))
