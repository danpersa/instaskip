(ns instaskip.innkeeper-paths-client
  (:require [clojure.spec :as s]
            [instaskip.innkeeper-client :as ik]))

; specs for create-path args
(s/def :k/host string?)
(s/def :k/hosts (s/* :k/host))
(s/def :k/path-with-hosts (s/keys :req-un [:k/uri :k/hosts :k/owned-by-team]))

(defn- transform-hosts-to-ids [hosts]
  (let [hosts-to-ids (ik/hosts-to-ids)]
    (vec (map (fn [host] (hosts-to-ids host)) hosts))))

(defn- path-with-hosts->path-with-host-ids [path]
  {:uri           (path :uri)
   :host-ids      (transform-hosts-to-ids (path :hosts))
   :owned-by-team (path :owned-by-team)})

(s/fdef create-path
        :args (s/cat :path :k/path-with-hosts)
        :ret :ik/response-path)

(defn create-path
  "Creates a path. The path has host strings instead of host ids.
   Returns a map representing the path with the id."
  [path]
  (let [path-with-host-ids (path-with-hosts->path-with-host-ids path)]
    (ik/post-path path-with-host-ids)))

(s/instrument #'create-path)

(defn path-uris-to-paths []
  (->> (ik/get-paths)
       (map (fn [path] [(path :uri) path]))
       (into {})))
