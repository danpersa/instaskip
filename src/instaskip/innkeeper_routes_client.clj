(ns instaskip.innkeeper-routes-client
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [instaskip.case-utils :refer [snake-to-hyphen-keyword]]
            [instaskip.innkeeper-client :as innkeeper]
            [instaskip.innkeeper-paths-client :as innkeeper-paths]
            [clojure.tools.logging :as log]))



(defn post-route [route])


(defn- create-route-with-existing-path [route path]
  (let [path-id (path :id)
        route-with-id (assoc route :path-id path-id)]
    (post-route route-with-id)))

(defn create-route
  "Gets a map with a path and a route as a parameter.
  The path might not exist. The hosts for the route are strings instead of ids.
  The route doesn't have a path id."
  [route-with-path]

  (log/info "Create route " route-with-path)

  (let [route (route-with-path :route)
        path (route-with-path :path)
        current-path ((innkeeper-paths/path-uris-to-paths) (path :uri))]
    (if (nil? current-path)
      (let [current-path (innkeeper-paths/post-path path)]
        (create-route-with-existing-path route current-path))
      (create-route-with-existing-path route path))
    )
  )

(comment
  (create-route {:route
                       {:name                "theRoute",
                        :route               {:predicates [{:name "Method"
                                                            :args [{:type :string :value "GET"}]}]
                                              :filters    [{:name "filter1"
                                                            :args []}
                                                           {:name "filter2"
                                                            :args [{:type :string :value "value"}]}]
                                              :endpoint   ""}
                        :uses-common-filters true}
                 :path {:uri   "/path/example"
                        :hosts ["service.com"]
                        }}))