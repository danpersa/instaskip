(ns instaskip.innkeeper-client
  (:require [clj-http.client :as http]
            [instaskip.json :as json]
            [clojure.spec :as s]
            [clojure.tools.logging :as log]))

;; config related defs

(def innkeeper-url "http://localhost:8080")
(def hosts-url (str innkeeper-url "/hosts"))
(def paths-url (str innkeeper-url "/paths"))
(def routes-url (str innkeeper-url "/routes"))

(def read-token (str "Bearer " "token-user~1-employees-route.read"))
(def write-token (str "Bearer " "token-user~1-employees-route.write"))
(def admin-token (str "Bearer " "token-user~1-employees-route.admin"))


;; host related functions

(defn get-hosts []

  (-> (http/get hosts-url {:headers   {"Authorization" read-token}
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
    (http/get (str paths-url "/" id)
              {:accept    :json
               :headers   {"Authorization" read-token}
               :insecure? true})))

(s/instrument #'get-path)

(s/fdef post-path
        :args (s/cat :path :k/innkeeper-request-path)
        :ret :k/innkeeper-response-path)

(defn post-path
  "Posts a path to innkeeper. Returns the created path."
  [path]

  (log/info "Create path: " path)
  (-> (http/post paths-url {:body         (json/clj->json path)
                            :accept       :json
                            :content-type :json
                            :headers      {"Authorization" admin-token}
                            :insecure?    true})
      json/extract-body))

(s/instrument #'post-path)

(defn get-paths []
  (-> (http/get (str paths-url) {:accept    :json
                                 :headers   {"Authorization" read-token}
                                 :insecure? true})
      json/extract-body))


;; route related functions

(defn post-route [route]

  (log/info "Create route " route)
  (-> (http/post routes-url {:body         (json/clj->json route)
                             :accept       :json
                             :content-type :json
                             :headers      {"Authorization" admin-token}
                             :insecure?    true})
      json/extract-body))