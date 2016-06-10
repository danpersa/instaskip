(ns instaskip.innkeeper-hosts-client
  (:require [clj-http.client :as client]
            [instaskip.innkeeper-config :as ic]
            [clojure.spec :as s]
            [instaskip.json :as json]))


(def hosts-url (str ic/innkeeper-url "/hosts"))

(defn- hosts-response []

  (client/get hosts-url {:headers   {"Authorization" ic/read-token}
                         :insecure? true}))

(defn- to-tuple-fn [id-to-host]
  [(id-to-host :name) (Integer. (id-to-host :id))])


(s/def :k/hosts-to-ids (s/map-of string? integer?))
(s/fdef hosts-to-ids
        :ret :k/hosts-to-ids)

(defn hosts-to-ids
  "Returns map from hosts to host ids"
  []

  (->> (hosts-response)
       json/extract-body
       (map to-tuple-fn)
       (into {})))

(s/instrument #'hosts-to-ids)
