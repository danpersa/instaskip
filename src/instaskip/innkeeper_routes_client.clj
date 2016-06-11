(ns instaskip.innkeeper-routes-client
  (:require [instaskip.innkeeper-paths-client :as ip]
            [instaskip.innkeeper-client :as ik]))

(defn- create-route-with-existing-path [route path]
  (let [path-id (path :id)
        route-with-id (assoc route :path-id path-id)]
    (ik/post-route route-with-id)))

(defn create-route
  "Gets a map with a path and a route as a parameter.
  The path might not exist. The hosts for the route are strings.
  The route doesn't have a path id."
  [route-with-path]

  (let [route (route-with-path :route)
        path (route-with-path :path)
        current-path ((ip/path-uris-to-paths) (path :uri))]
    (if (nil? current-path)
      (let [current-path (ip/create-path path)]
        (create-route-with-existing-path route current-path))
      (create-route-with-existing-path route current-path))
    )
  )

(comment
  (create-route {:route
                       {:name                "theRoute1",
                        :route               {:predicates [{:name "Method"
                                                            :args [{:type :string :value "GET"}]}]
                                              :filters    [{:name "filter1"
                                                            :args []}
                                                           {:name "filter2"
                                                            :args [{:type :string :value "value"}]}]
                                              :endpoint   ""}
                        :uses-common-filters true}
                 :path {:uri           "/path/example"
                        :hosts         ["service.com"]
                        :owned-by-team "theTeam"
                        }}))
