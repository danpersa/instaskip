(ns instaskip.innkeeper-client
  (:require [clj-http.client :as http]
            [instaskip.innkeeper-config :as ic]
            [instaskip.json :as json]))

(defn get-hosts []

  (-> (http/get ic/hosts-url {:headers     {"Authorization" ic/read-token}
                                :insecure? true})
      json/extract-body))