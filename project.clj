(defproject org.clojars.danpersa/instaskip "0.2.6"
  :description "Transforms from eskip to json to eskip."
  :url "https://github.com/danpersa/instaskip"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure       "1.9.0-alpha5"]
                 [instaparse                "1.4.1"]
                 [rhizome                   "0.2.5"]
                 [org.clojure/data.json     "0.2.6"]
                 [org.clojure/core.match    "0.3.0-alpha4"]
                 [clj-http                  "3.1.0"]
                 [funcool/cats              "1.2.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [me.raynes/fs              "1.4.6"]
                 [org.clojure/tools.cli     "0.3.5"]
                 [defun                     "0.3.0-alapha"]]
  :main ^:skip-aot instaskip.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev     {:source-paths ["dev"]
                       :repl-options {:init-ns instaskip.eskip}
                       :plugins      [[lein-midje                  "3.2"]]
                       :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                      [org.clojure/java.classpath  "0.2.3"]
                                      [criterium                   "0.4.4"]
                                      [midje                       "1.9.0-SNAPSHOT"]
                                      [clj-http-fake               "1.0.2"]]}}
  :aot [instaskip.eskip]
  ;; :jvm-opts ^:replace ["-Dclojure.compiler.direct-linking=true"]
  )
