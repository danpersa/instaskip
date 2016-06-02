(ns instaskip.innkeeper-client
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [instaskip.case-utils :refer [snake-to-hyphen-keyword]]))


(def innkeeper-url "http://0.0.0.0:8080")
(def hosts-url (str innkeeper-url "/hosts"))
(def paths-url (str innkeeper-url "/paths"))
(def token (str "Bearer" "token-user~1-employees-route.read"))

(def ^{:private true} hosts-response

  (client/get hosts-url {:accept  :json
                         :headers {"Authorization" token}}))

(defn- hosts-body [response]

  (json/read-str (response :body)
                 :key-fn snake-to-hyphen-keyword))

(def ^{:private true} to-tuple-fn

  (fn [id-to-host] [(id-to-host :name) (id-to-host :id)]))


(def hosts-to-ids-map
  "A map in the format:

  {host1.com 1
   host2.com 2}"

  (->> hosts-response
       hosts-body
       (map to-tuple-fn)
       (into {})))

hosts-to-ids-map

(defn- path-response [path-id]
  (client/get (str paths-url "/" path-id) {:accept  :json
                                           :headers {"Authorization" token}}))

(defn path
  "Calls innkeeper and returns a map containing the path with the specified id"
  [id] (->> (path-response id)
                     hosts-body))

(path 100)

