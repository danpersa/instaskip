(ns instaskip.innkeeper-client
  (:require [clj-http.client :as http]
            [instaskip.json :as json]
            [clojure.spec :as s]
            [clojure.tools.logging :as log]))

;; config related defs

(def innkeeper-url "http://localhost:9080")
(def hosts-url (str innkeeper-url "/hosts"))
(def paths-url (str innkeeper-url "/paths"))
(def routes-url (str innkeeper-url "/routes"))

(def read-token (str "Bearer " "token-user~1-employees-route.read"))
(def write-token (str "Bearer " "token-user~1-employees-route.write"))
(def admin-token (str "Bearer " "token-user~1-employees-route.admin"))


;; host related functions
(s/def :ik/id integer?)
(s/def :ik/host (s/keys
                  :req-un
                  [:ik/id
                   :ik/name]))
(s/def :ik/response-hosts (s/* :ik/host))

(s/fdef get-hosts :ret :ik/response-hosts)
(defn get-hosts []

  (-> (http/get hosts-url {:headers   {"Authorization" read-token}
                           :insecure? true})
      json/extract-body))
(s/instrument #'get-hosts)

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

(s/fdef get-path :ret :ik/response-path)

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
        :args (s/cat :path :ik/request-path)
        :ret :ik/response-path)

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

(s/def :ik/response-paths (s/* :ik/response-path))

(s/fdef get-paths :ret :ik/response-paths)

(defn get-paths []
  (-> (http/get (str paths-url) {:accept    :json
                                 :headers   {"Authorization" read-token}
                                 :insecure? true})
      json/extract-body))

(s/instrument #'get-paths)

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

(s/def :ik-out/route (s/keys :req-un
                             [:ik/predicates
                              :ik/filters]
                             :opt-un
                             [:ik/endpoint]))

(s/def :ik/request-route (s/keys :req-un
                                 [:ik/name
                                  :ik-in/route
                                  :ik/uses-common-filters
                                  :ik/path-id]
                                 :opt-un
                                 [:ik/activate-at
                                  :ik/disable-at
                                  :ik/description]))

(s/def :ik/response-route (s/keys :req-un
                                  [:ik/name
                                   :ik-out/route
                                   :ik/uses-common-filters
                                   :ik/path-id
                                   :ik/created-by
                                   :ik/created-at
                                   :ik/activate-at]
                                  :opt-un
                                  [:ik/description
                                   :ik/disable-at]))

(s/fdef post-route
        :args (s/cat :route :ik/request-route)
        :ret :ik/response-route)

(defn post-route [route]

  (log/info "Create route " route)
  (-> (http/post routes-url {:body         (json/clj->json route)
                             :accept       :json
                             :content-type :json
                             :headers      {"Authorization" admin-token}
                             :insecure?    true})
      json/extract-body))

(s/instrument #'post-route)