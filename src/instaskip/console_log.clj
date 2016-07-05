(ns instaskip.console-log
  (:require [clojure.string :as str]
            [clojure.stacktrace :as cs]))

(defn info [& args] (println (str/join " " args)))

(defn error [ex & args]
  (println (str "ERROR: Something went wrong: "
                (.getMessage ex)
                (str/join " " args))))
