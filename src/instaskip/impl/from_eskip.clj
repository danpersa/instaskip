(ns instaskip.impl.from-eskip
  (:require [instaparse.core :as insta]
            [clojure.data.json :as json]))

(def ^{:private true :testable true} eskip-routes-parser
  "returns an parser for eskip routes"
  (insta/parser
    "
     routes           = route+
     route            = name <':'> predicates <'->'> filters endpoint <';'>
     name             = #'[a-zA-Z0-9_]+'
     filters          = (filter <'->'> )*
     predicates       = predicate (<'&&'> predicate)* | star
     predicate        = predicate-name <'('> predicate-arg? (<','> predicate-arg)* <')'>
     predicate-name   = #'[a-zA-Z0-9]*'
     predicate-arg    = string-arg | number | regexval
     filter           = filter-name <'('> filter-arg? (<','> filter-arg)* <')'>
     filter-name      = #'[a-zA-Z0-9]*'
     filter-arg       = string-arg | number | regexval
     endpoint         = string | shunt
     shunt            = #'<shunt>'
     star             = #'\\*'
     string           = <quote> #'[^\"]*' <quote>
     string-arg       = string
     quote            = #'\"'
     number           = decimal-number | int-number
     decimal-number   = #'[0-9]+\\.[0-9]*'
     int-number       = #'[0-9]+'
     regexval         = #'\\/\\^[^$]*\\$/'
     "
    :auto-whitespace :standard
    :output-format :hiccup))

(defn- ^{:testable true}
name-and-args-to-map [name & args]

  {:name name :args (vec args)})

(defn- args-to-map [name & values] (vec `(~name ~(vec values))))

(defn- transform-ast-to-map
  "Tranforms the AST returned after parsing eskip routes to a clojure map"
  [ast]

  (insta/transform
    {:shunt          (fn [_] "")
     :string-arg     (fn [string] {:value string :type "string"})
     :string         identity
     :star           (fn [star] {:name star :args []})
     :int-number     identity
     :decimal-number identity
     :number         (fn [number] {:value number :type "number"})
     :regexval       (fn [regexval] {:value (str regexval) :type "regex"})
     :filter-name    identity
     :filter-arg     identity
     :filter         name-and-args-to-map
     :filters        (partial args-to-map :filters)
     :predicate-name identity
     :predicate-arg  identity
     :predicate      name-and-args-to-map
     :predicates     (partial args-to-map :predicates)
     :route          (fn [& args] (into {} (concat args)))
     :routes         (fn [& args] (vec (concat args)))
     }
    ast))

(defn eskip->json
  "Transforms an eskip routes string to a json array string"
  [eskip-routes]

  (-> eskip-routes
      eskip-routes-parser
      transform-ast-to-map
      json/write-str))

(defn eskip->maps
  "Transforms an eskip routes string to an array of maps"
  [eskip-routes]

  (-> eskip-routes
      eskip-routes-parser
      transform-ast-to-map))

(defn eskip->map
  "Transforms an eskip route string to a map"
  [eskip-routes]

  (-> eskip-routes
      eskip-routes-parser
      transform-ast-to-map
      first))

(defn single-eskip->json
  "Transforms an eskip route string to a json string"
  [eskip-routes]

  (-> eskip-routes
      eskip-routes-parser
      transform-ast-to-map
      first
      json/write-str))
