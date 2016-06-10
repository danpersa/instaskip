(ns instaskip.json
  (:require [clojure.data.json :as json]))

(defn clj->json [clj]
  (json/write-str clj
                  :key-fn instaskip.case-utils/hyphen-keyword-to-snake))
