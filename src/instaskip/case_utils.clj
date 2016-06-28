(ns instaskip.case-utils
  (:require [clojure.string :as string]
            [clojure.walk :as walk]))

(defn snake->hyphen
  "Transforms a string from snake case to hyphen case."
  [s]

  (string/replace s #"_" "-"))

(defn snake->hyphen-keyword
  "Transforms a string from snake case to hyphen case keyword."
  [s]

  ((comp keyword snake->hyphen) s))

(defn hyphen-keyword->snake
  "Transforms a keyword from hyphen case to snake case."
  [s]

  (string/replace (name s) #"-" "_"))

(defn hyphen-keyword-map-keys->snake
  "Recursively transforms all map keys from hyphen keywords to snake case string."
  [m]

  (let [f (fn [[k v]]
            (if (keyword? k)
              [(hyphen-keyword->snake k) v]
              [k v]))]
    ;; only apply to maps
    (walk/postwalk
      (fn [x] (if (map? x) (into {} (map f x)) x))
      m)))