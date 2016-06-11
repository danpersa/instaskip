(ns instaskip.innkeeper-paths-client
  (:require [clj-http.client :as http]
            [clojure.tools.logging :as log]
            [instaskip.innkeeper-config :as ic]
            [instaskip.innkeeper-hosts-client :as ih]
            [clojure.spec :as s]
            [instaskip.json :as json]))

(def paths-url (str ic/innkeeper-url "/paths"))

; spec for innkeeper path
(s/def :k/id integer?)
(s/def :k/host-ids (s/* :k/id))
(s/def :k/innkeeper-response-path (s/keys
                                    :req-un
                                    [:k/id
                                     :k/uri
                                     :k/host-ids
                                     :k/owned-by-team
                                     :k/created-by
                                     :k/created-at]))

; specs for create-path args
(s/def :k/host string?)
(s/def :k/hosts (s/* :k/host))
(s/def :k/path-with-hosts (s/keys :req-un [:k/uri :k/hosts :k/owned-by-team]))

(s/fdef get-path :ret :k/innkeeper-response-path)

(defn get-path
  "Calls innkeeper and returns the path with the specified id"
  [id]

  (json/extract-body
    (http/get (str paths-url "/" id)
              {:accept    :json
               :headers   {"Authorization" ic/read-token}
               :insecure? true})))
(s/instrument #'get-path)

(defn- transform-hosts-to-ids [hosts]
  (let [hosts-to-ids (ih/hosts-to-ids)]
    (vec (map (fn [host] (hosts-to-ids host)) hosts))))

(defn- path-with-hosts->path-with-host-ids [path]
  {:uri           (path :uri)
   :host-ids      (transform-hosts-to-ids (path :hosts))
   :owned-by-team (path :owned-by-team)})

; specs for post-path args
(s/def :k/innkeeper-request-path (s/keys :req-un [:k/uri :k/host-ids :k/owned-by-team]))
(s/fdef post-path
        :args (s/cat :path :k/innkeeper-request-path)
        :ret :k/innkeeper-response-path)

(defn- post-path
  "Posts a path to innkeeper. Returns the created path."
  [path]

  (log/info "Create path: " path)
  (-> (http/post paths-url {:body         (json/clj->json path)
                            :accept       :json
                            :content-type :json
                            :headers      {"Authorization" ic/admin-token}
                            :insecure?    true})
      json/extract-body))

(s/instrument #'post-path)


(s/fdef create-path
        :args (s/cat :path :k/path-with-hosts)
        :ret :k/innkeeper-response-path)

(defn create-path
  "Creates a path. The path has host strings instead of host ids.
   Returns a map representing the path with the id."
  [path]
  (let [path-with-host-ids (path-with-hosts->path-with-host-ids path)]
    (post-path path-with-host-ids)))

(s/instrument #'create-path)

(defn path-uris-to-paths []
  (->> (http/get (str paths-url) {:accept    :json
                                  :headers   {"Authorization" ic/read-token}
                                  :insecure? true})
       json/extract-body
       (map (fn [path] [(path :uri) path]))
       (into {})))
