(defproject org.clojars.danpersa/instaskip "0.1.7"
  :description "Transforms from eskip to json to eskip."
  :url "https://github.com/danpersa/instaskip"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure   "1.8.0"]
                 [instaparse            "1.4.1"]
                 [rhizome               "0.2.5"]
                 [org.clojure/data.json "0.2.6"]]
  :main ^:skip-aot instaskip.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}}
  :jvm-opts ^:replace ["-Dclojure.compiler.direct-linking=true"]
  )
