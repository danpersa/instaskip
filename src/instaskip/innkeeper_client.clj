(ns instaskip.innkeeper-client
  (:require
            [clojure.data.json :as json]
            [instaskip.case-utils :refer [snake-to-hyphen-keyword]]))


(def innkeeper-url "http://localhost:8080")

(def read-token (str "Bearer" "token-user~1-employees-route.read"))
(def write-token (str "Bearer" "token-user~1-employees-route.write"))

(defn extract-body [response]

  (json/read-str (response :body)
                 :key-fn snake-to-hyphen-keyword))

