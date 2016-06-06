(ns instaskip.innkeeper-hosts-client
  (:require [clj-http.client :as client]
            [instaskip.case-utils :refer [snake-to-hyphen-keyword]]
            [instaskip.innkeeper-client :as innkeeper]))


(def ^{:private true} hosts-url (str innkeeper/innkeeper-url "/hosts"))


(def ^{:private true} hosts-response

  (client/get hosts-url {:headers {"Authorization" innkeeper/read-token}}))

(def ^{:private true} to-tuple-fn

  (fn [id-to-host] [(id-to-host :name) (Integer. (id-to-host :id))]))


(defn hosts-to-ids-map
  "A map in the format:

  {host1.com 1
   host2.com 2}"

  []

  (->> hosts-response
       innkeeper/extract-body
       (map to-tuple-fn)
       (into {})))

(comment
  (hosts-to-ids-map))