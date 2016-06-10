(ns instaskip.json
  (:require [clojure.data.json :as json]
            [instaskip.case-utils :as cu]))

(defn clj->json [clj]
  (json/write-str clj
                  :key-fn cu/hyphen-keyword-to-snake))

(defn extract-body [response]
  (json/read-str (response :body)
                 :key-fn cu/snake-to-hyphen-keyword))
