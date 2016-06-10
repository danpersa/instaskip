(ns instaskip.innkeeper-hosts-client
  (:require [clj-http.client :as client]
            [instaskip.case-utils :refer [snake-to-hyphen-keyword]]
            [instaskip.innkeeper-client :as innkeeper]
            [clojure.spec :as s]))


(def hosts-url (str innkeeper/innkeeper-url "/hosts"))


(defn- hosts-response []

  (client/get hosts-url {:headers {"Authorization" innkeeper/read-token}
                         :insecure? true}))

(defn- to-tuple-fn [id-to-host]
  [(id-to-host :name) (Integer. (id-to-host :id))])

(defn hosts-to-ids
  "A map in the format:

  {\"host1.com\" 1
   \"host2.com\" 2}"

  []

  (->> (hosts-response)
       innkeeper/extract-body
       (map to-tuple-fn)
       (into {})))

(comment
  (hosts-to-ids))
