(ns instaskip.console-log
  (:require [clojure.string :as str]))

(defn info [& args] (println (str/join " " args)))

(defn error [& args] (println (str "ERROR: Something went wrong: " (str/join " " args))))
