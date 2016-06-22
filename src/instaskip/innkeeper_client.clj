(ns instaskip.innkeeper-client
  (:require [clj-http.client :as http]
            [instaskip.json :as json]
            [clojure.spec :as s]
            [clojure.tools.logging :as log]
            [defun :refer [defun]]))

;; config related defs
(defn hosts-url [innkeeper-url] (str innkeeper-url "/hosts"))
(defn paths-url [innkeeper-url] (str innkeeper-url "/paths"))
(defn routes-url [innkeeper-url] (str innkeeper-url "/routes"))

(defn- build-token-header [token] (str "Bearer " token))

(def read-token (build-token-header "token-user~1-employees-route.read"))
(def write-token (build-token-header "token-user~1-employees-route.write"))
(def admin-token (build-token-header "token-user~1-employees-route.admin"))

(s/def :ik/config (s/keys :req-un
                          [:ik/innkeeper-url
                           :ik/oauth-token]))

;; host related functions
(s/def :ik/id integer?)
(s/def :ik/host (s/keys
                  :req-un
                  [:ik/id
                   :ik/name]))
(s/def :ik/response-hosts (s/* :ik/host))

(s/fdef get-hosts :args (s/cat :config :ik/config)
        :ret :ik/response-hosts)

(defn get-hosts [{:keys [innkeeper-url oauth-token]}]

  (-> (http/get (hosts-url innkeeper-url)
                {:headers   {"Authorization" (build-token-header oauth-token)}
                 :insecure? true})
      json/extract-body))

(s/instrument #'get-hosts)

(defn- to-tuple-fn [id-to-host]
  [(id-to-host :name) (Integer. (id-to-host :id))])

(s/def :k/hosts-to-ids (s/map-of string? integer?))
(s/fdef hosts-to-ids
        :args (s/cat :config :ik/config)
        :ret :k/hosts-to-ids)

(defn hosts-to-ids
  "Returns map from hosts to host ids"
  [config]

  (->> (get-hosts config)
       (map to-tuple-fn)
       (into {})))

(s/instrument #'hosts-to-ids)


;; path related functions

(s/def :ik/host-ids (s/* :ik/id))
(s/def :ik/response-path (s/keys
                           :req-un
                           [:ik/id
                            :ik/uri
                            :ik/host-ids
                            :ik/owned-by-team
                            :ik/created-by
                            :ik/created-at]))

(s/def :ik/request-path (s/keys
                          :req-un
                          [:ik/uri
                           :ik/host-ids
                           :ik/owned-by-team]))

(s/fdef get-path
        :args (s/cat :id :ik/id :config :ik/config)
        :ret :ik/response-path)

(defn get-path
  "Calls innkeeper and returns the path with the specified id"
  [id {:keys [innkeeper-url oauth-token]}]

  (json/extract-body
    (http/get (str (paths-url innkeeper-url) "/" id)
              {:accept    :json
               :headers   {"Authorization" (build-token-header oauth-token)}
               :insecure? true})))

(s/instrument #'get-path)

(s/fdef post-path
        :args (s/cat :path :ik/request-path :config :ik/config)
        :ret :ik/response-path)

(defn post-path
  "Posts a path to innkeeper. Returns the created path."
  [path {:keys [innkeeper-url oauth-token]}]

  (log/debug "Create path: " path)
  (-> (http/post (paths-url innkeeper-url)
                 {:body         (json/clj->json path)
                  :accept       :json
                  :content-type :json
                  :headers      {"Authorization"
                                 (build-token-header oauth-token)}
                  :insecure?    true})
      json/extract-body))

(s/instrument #'post-path)

(s/def :ik/response-paths (s/* :ik/response-path))

(s/fdef get-paths :args (s/cat :config :ik/config)
        :ret :ik/response-paths)

(defn get-paths [{:keys [innkeeper-url oauth-token]}]
  (-> (http/get (paths-url innkeeper-url)
                {:accept    :json
                 :headers   {"Authorization"
                             (build-token-header oauth-token)}
                 :insecure? true})
      json/extract-body))

(s/instrument #'get-paths)

(defn path-uris-to-paths
  "Returns a map from path uris to paths"

  [config]
  (->> (get-paths config)
       (map (fn [path] [(path :uri) path]))
       (into {})))


;; route related functions
(s/def :ik/uses-common-filters boolean?)
(s/def :ik/path-id integer?)
(s/def :ik-in/route (s/keys :opt-un
                            [:ik/predicates
                             :ik/filters
                             :ik/endpoint]))

(defn type? [x] (#{"string" "regex" "number"} x))

(s/def :ik/type type?)

(s/def :ik/arg (s/keys :req-un
                       [:ik/value
                        :ik/type]))

(s/def :ik/args (s/* :ik/arg))

(s/def :ik/filter-or-predicate (s/keys :req-un
                                       [:ik/name
                                        :ik/args]))

(s/def :ik/predicates (s/* :ik/filter-or-predicate))
(s/def :ik/filters (s/* :ik/filter-or-predicate))


(s/def :ik/request-route (s/keys :req-un
                                 [:ik/name
                                  :ik/uses-common-filters
                                  :ik/path-id]
                                 :opt-un
                                 [:ik/activate-at
                                  :ik/disable-at
                                  :ik/description
                                  :ik/predicates
                                  :ik/filters
                                  :ik/endpoint]))

(s/def :ik/response-route (s/keys :req-un
                                  [:ik/name
                                   :ik/predicates
                                   :ik/filters
                                   :ik/uses-common-filters
                                   :ik/path-id
                                   :ik/created-by
                                   :ik/created-at
                                   :ik/activate-at]
                                  :opt-un
                                  [:ik/description
                                   :ik/disable-at
                                   :ik/endpoint]))

(s/fdef post-route
        :args (s/cat :route :ik/request-route :config :ik/config)
        :ret :ik/response-route)

(defn post-route [route {:keys [innkeeper-url oauth-token]}]

  (log/debug "Create route " route)
  (-> (http/post (routes-url innkeeper-url)
                 {:body         (json/clj->json route)
                  :accept       :json
                  :content-type :json
                  :headers      {"Authorization" (build-token-header oauth-token)}
                  :insecure?    true})
      json/extract-body))

(s/instrument #'post-route)
