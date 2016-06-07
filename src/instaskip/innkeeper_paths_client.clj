(ns instaskip.innkeeper-paths-client
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [instaskip.case-utils :refer [snake-to-hyphen-keyword]]
            [cats.monad.exception :as exc]
            [cats.core :as cats]
            [clojure.tools.logging :as log]
            [instaskip.innkeeper-client :as innkeeper]
            [instaskip.innkeeper-hosts-client :as innkeeper-hosts])
  (:import [cats.monad.exception Success]
           [cats.monad.exception Failure]))

(def ^{:private true} paths-url (str innkeeper/innkeeper-url "/paths"))

(defn- path-response
  "Tries to get a path from innkeeper."
  [path-id]

  (exc/try-on
    (client/get (str paths-url "/" path-id) {:accept  :json
                                             :headers {"Authorization" innkeeper/read-token}})))

(defn get-path
  "Calls innkeeper and returns a map containing the path with the specified id"
  [id]

  (cats/fmap innkeeper/extract-body (path-response id)))

(defn- transform-hosts-to-ids [hosts]
  (let [hosts-to-ids (innkeeper-hosts/hosts-to-ids-map)]
    (vec (map (fn [host] (hosts-to-ids host)) hosts))))

(comment
  (transform-hosts-to-ids ["service.com" "m.service.com"]))

(defn- transform-path-with-hosts-to-ids [path]
  {:uri      (path :uri)
   :host_ids (transform-hosts-to-ids (path :hosts))})

(comment
  (transform-path-with-hosts-to-ids {:uri   "/uri"
                                     :hosts ["service.com" "m.service.com"]}))

(defn post-path
  "Posts a path to innkeeper."
  [path]

  (client/post paths-url {:body         (json/write-str path)
                          :accept       :json
                          :content-type :json
                          :headers      {"Authorization" innkeeper/write-token}}))

(comment (post-path {:uri "/uri-22" :host_ids [1 3 4]}))

(defn create-path
  "Creates a path. The path has host strings instead of host ids.
   Returns a map representing the path with the id."
  [path]
  (let [path-with-host-ids (transform-path-with-hosts-to-ids path)]
    (innkeeper/extract-body
      (post-path path-with-host-ids))))

(comment (create-path {:uri   "/uri-123"
                       :hosts ["service.com" "m.service.com"]}))

(defmulti unwrap-try class)

(defmethod unwrap-try Success [data]
  @data)

(defmethod unwrap-try Failure [data]
  (let [value (cats/extract data)]
    (log/error "There was an error" value)))

(defn path-uris-to-paths []
  (->> (client/get (str paths-url) {:accept  :json
                                    :headers {"Authorization" innkeeper/read-token}})
       innkeeper/extract-body
       (map (fn [path] [(path :uri) path]))
       (into {})))

(comment (path-uris-to-paths))
;(unwrap-try (get-path 99))
;(unwrap-try (get-path 1))

