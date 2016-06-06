(defproject org.clojars.danpersa/instaskip "0.2.6"
  :description "Transforms from eskip to json to eskip."
  :url "https://github.com/danpersa/instaskip"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure    "1.8.0"]
                 [instaparse             "1.4.1"]
                 [rhizome                "0.2.5"]
                 [org.clojure/data.json  "0.2.6"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [clj-http               "3.1.0"]
                 [funcool/cats           "1.2.1"]
                 [org.clojure/tools.logging "0.3.1"]]
  :main ^:skip-aot instaskip.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev     {:source-paths ["dev"]
                       :repl-options {:init-ns instaskip.eskip}
                       :plugins      [[lein-midje "3.2"]]
                       :dependencies [[org.clojure/tools.namespace "0.2.11"]
                                      [org.clojure/java.classpath  "0.2.3"]
                                      [criterium                   "0.4.4"]
                                      [midje                       "1.8.3"]]}}
  :aot [instaskip.eskip]
  :jvm-opts ^:replace ["-Dclojure.compiler.direct-linking=true"]
  )
