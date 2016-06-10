(ns instaskip.innkeeper-paths-client
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [instaskip.case-utils :refer [snake-to-hyphen-keyword]]
            [cats.monad.exception :as exc]
            [cats.core :as cats]
            [clojure.tools.logging :as log]
            [instaskip.innkeeper-client :as innkeeper]
            [instaskip.innkeeper-hosts-client :as innkeeper-hosts]
            [clojure.spec :as s]
            [instaskip.json :refer [clj->json]]
            )
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
  (let [hosts-to-ids (innkeeper-hosts/hosts-to-ids)]
    (vec (map (fn [host] (hosts-to-ids host)) hosts))))

(comment
  (transform-hosts-to-ids ["service.com" "m.service.com"]))


(defn- path-with-hosts->path-with-host-ids [path]
  {:uri           (path :uri)
   :host-ids      (transform-hosts-to-ids (path :hosts))
   :owned-by-team (path :owned-by-team)})

(comment
  (path-with-hosts->path-with-host-ids {:uri           "/uri"
                                        :hosts         ["service.com" "m.service.com"]
                                        :owned-by-team "team-1"}))

(defn- post-path
  "Posts a path to innkeeper."
  [path]

  (log/info "Create path: " path)
  (client/post paths-url {:body         (clj->json path)
                          :accept       :json
                          :content-type :json
                          :headers      {"Authorization" innkeeper/admin-token}
                          :insecure?    true}))

(comment
  (post-path {:uri "/uri-22" :host-ids [1 3 4] :owned-by-team "theTeam"}))

; spec for args
(s/def :k/host string?)
(s/def :k/hosts (s/* :k/host))
(s/def :k/path-with-hosts (s/keys :req-un [:k/uri :k/hosts :k/owned-by-team]))

; spec for ret
(s/def :k/id integer?)
(s/def :k/host-ids (s/* :k/id))
(s/def :k/created-path (s/keys
                         :req-un
                         [:k/id
                          :k/uri
                          :k/host-ids
                          :k/owned-by-team
                          :k/created-by
                          :k/created-at]))
(s/fdef create-path
        :args (s/cat :path :k/path-with-hosts)
        :ret :k/created-path)

(defn create-path
  "Creates a path. The path has host strings instead of host ids.
   Returns a map representing the path with the id."
  [path]
  (let [path-with-host-ids (path-with-hosts->path-with-host-ids path)]
    (innkeeper/extract-body
      (post-path path-with-host-ids))))

(s/instrument #'create-path)

(comment (create-path {:uri           "/hello2"
               :hosts         ["service.com"]
               :owned-by-team "someTeam"}))

(defmulti unwrap-try class)

(defmethod unwrap-try Success [data]
  @data)

(defmethod unwrap-try Failure [data]
  (let [value (cats/extract data)]
    (log/error "There was an error" value)))

(defn path-uris-to-paths []
  (->> (client/get (str paths-url) {:accept    :json
                                    :headers   {"Authorization" innkeeper/read-token}
                                    :insecure? true})
       innkeeper/extract-body
       (map (fn [path] [(path :uri) path]))
       (into {})))

(comment (path-uris-to-paths))
