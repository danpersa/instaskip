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
