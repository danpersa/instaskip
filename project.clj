(defproject instaskip "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure   "1.8.0"]
                 [instaparse            "1.4.1"]
                 [rhizome               "0.2.5"]
                 [org.clojure/data.json "0.2.6"]]
  :main ^:skip-aot instaskip.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
