(ns instaskip.innkeeper-client
  (:require [clj-http.client :as http]
            [instaskip.innkeeper-config :as ic]
            [instaskip.json :as json]
            [clojure.spec :as s]
            [clojure.tools.logging :as log]))

;; host related functions

(defn get-hosts []

  (-> (http/get ic/hosts-url {:headers   {"Authorization" ic/read-token}
                              :insecure? true})
      json/extract-body))

;; path related functions

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

(s/def :k/innkeeper-request-path (s/keys
                                   :req-un
                                   [:k/uri
                                    :k/host-ids
                                    :k/owned-by-team]))

(s/fdef get-path :ret :k/innkeeper-response-path)

(defn get-path
  "Calls innkeeper and returns the path with the specified id"
  [id]

  (json/extract-body
    (http/get (str ic/paths-url "/" id)
              {:accept    :json
               :headers   {"Authorization" ic/read-token}
               :insecure? true})))

(s/instrument #'get-path)

(s/fdef post-path
        :args (s/cat :path :k/innkeeper-request-path)
        :ret :k/innkeeper-response-path)

(defn post-path
  "Posts a path to innkeeper. Returns the created path."
  [path]

  (log/info "Create path: " path)
  (-> (http/post ic/paths-url {:body         (json/clj->json path)
                               :accept       :json
                               :content-type :json
                               :headers      {"Authorization" ic/admin-token}
                               :insecure?    true})
      json/extract-body))

(s/instrument #'post-path)

(defn get-paths []
  (-> (http/get (str ic/paths-url) {:accept    :json
                                    :headers   {"Authorization" ic/read-token}
                                    :insecure? true})
      json/extract-body))