(ns instaskip.case-utils
  (:require [clojure.string :as string]))


(defn snake-to-hyphen
  "transforms a string from snake case to hyphen case"
  [s]

  (string/replace s #"_" "-"))

(defn snake-to-hyphen-keyword
  "transforms a string from snake case to hyphen case keyword"
  [s]

  ((comp keyword snake-to-hyphen) s))

(defn hyphen-keyword-to-snake
  "transforms a keyword from hyphen case to snake case"
  [s]

  (string/replace (name s) #"-" "_"))
