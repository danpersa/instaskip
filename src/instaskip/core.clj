(ns instaskip.core
  (:require [instaparse.core :as insta]
            [clojure.data.json :as json])
  (:gen-class))

(def eskip
  (insta/parser
    "
     routes           = route+
     route            = name <':'> predicates <'->'> filters backend <';'>
     name             = #'[a-zA-Z0-9]+'
     filters          = (filter <'->'> )*
     predicates       = predicate? (<'&&'> predicate)*
     predicate        = predicate-name <'('> predicate-arg? (<','> predicate-arg)* <')'>
     predicate-name   = #'[a-zA-Z0-9]*'
     predicate-arg    = string | number | regexval
     filter           = filter-name <'('> filter-arg? (<','> filter-arg)* <')'>
     filter-name      = #'[a-zA-Z0-9]*'
     filter-arg       = string | number
     backend          = string | shunt
     shunt            = #'<shunt>'
     string           = <quote> #'[^\"]*' <quote>
     quote            = #'\"'
     number           = #'[0-9]+\\.[0-9]*'
     regexval         = #'\\/\\^[^$]*\\$/'
     "
    :auto-whitespace :standard
    :output-format :hiccup))

(defn str->num [str]
  (let [n (read-string str)]
    (if (number? n) n nil)))

(defn call-eskip []
  (let [eskip-route "
                   hello: predicate1(/^.*$/) && predicate2(\"arg1\", 4.3)
                   -> filter1(\"arg1\")
                   -> filter2(\"arg1\", 4.3, \"arg2\")
                   -> filter3()
                   -> \"https://hello.com\";
                   hello1: -> <shunt>;"]
   (eskip eskip-route)))

(defn name-and-args-to-map [name & args] {:name name :args (vec args)})
(defn args-to-map [name & values] (hash-map name (vec values)))

(insta/transform
  {:shunt          (fn [sh] "")
   :string         identity
   :number         str->num
   :filter-name    identity
   :filter-arg     identity
   :filter         name-and-args-to-map
   :filters        (partial args-to-map :filters)
   :predicate-name identity
   :predicate-arg  identity
   :predicate      name-and-args-to-map
   }
  (call-eskip))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, Instaskip!")
  (println
    (eskip eskip-route))
  ;(insta/visualize
  ;  (eskip eskip-route))
  )
