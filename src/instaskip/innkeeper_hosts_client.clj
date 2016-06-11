(ns instaskip.innkeeper-hosts-client
  (:require [instaskip.innkeeper-client :as ik]
            [clojure.spec :as s]))

(defn- to-tuple-fn [id-to-host]
  [(id-to-host :name) (Integer. (id-to-host :id))])

(s/def :k/hosts-to-ids (s/map-of string? integer?))
(s/fdef hosts-to-ids
        :ret :k/hosts-to-ids)

(defn hosts-to-ids
  "Returns map from hosts to host ids"
  []

  (->> (ik/get-hosts)
       (map to-tuple-fn)
       (into {})))

(s/instrument #'hosts-to-ids)
