(ns instaskip.core
  (:require [instaparse.core :as insta])
  (:gen-class))

(def eskip
  (insta/parser
    "
     eskip-file       = eskip+
     eskip            = name <':'> predicates <'->'> (filter <'->'> )* backend <';'>
     name             = #'[a-zA-Z0-9]+'
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
    :auto-whitespace :standard))


(let [eskip-route "
                   hello: predicate1(/^.*$/) && predicate2(\"arg1\", 4.3)
                   -> filter1(\"arg1\")
                   -> filter2(\"arg1\", 4.3, \"arg2\")
                   -> filter3()
                   -> <shunt>;
                   hello1: <shunt>;"]
  (defn -main
    "I don't do a whole lot ... yet."
    [& args]
    (println "Hello, Instaskip!")
    (println (eskip eskip-route))
    (insta/visualize
      (eskip eskip-route))))
