(ns instaskip.innkeeper-hosts-client
  (:require [clj-http.client :as client]
            [instaskip.case-utils :refer [snake-to-hyphen-keyword]]
            [instaskip.innkeeper-client :as innkeeper]
            [clojure.spec :as s]))


(def hosts-url (str innkeeper/innkeeper-url "/hosts"))


(defn- hosts-response []

  (client/get hosts-url {:headers   {"Authorization" innkeeper/read-token}
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
       innkeeper/extract-body
       (map to-tuple-fn)
       (into {})))

(s/instrument #'hosts-to-ids)
