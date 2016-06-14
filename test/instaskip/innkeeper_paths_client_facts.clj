(ns instaskip.innkeeper-paths-client-facts
  (:require
    [instaskip.innkeeper-paths-client :refer :all]
    [midje.sweet :refer :all]
    [midje.util :refer [testable-privates]]
    [instaskip.json :as json]
    [clj-http.fake :refer [with-fake-routes]]
    [instaskip.innkeeper-client :as ik]))

(testable-privates instaskip.innkeeper-paths-client
                   path-with-hosts->path-with-host-ids
                   transform-hosts-to-ids)

(fact "transforms path with hosts to path with host ids"

      (path-with-hosts->path-with-host-ids
        {:uri           "/uri"
         :hosts         ["service.com"]
         :owned-by-team "team-1"}) =>
      {:uri           "/uri"
       :host-ids      [1]
       :owned-by-team "team-1"}
      (provided
        (#'instaskip.innkeeper-paths-client/transform-hosts-to-ids ["service.com"]) => [1]))

(fact "transforms a list of hosts to a list of ids"

      (transform-hosts-to-ids ["service"]) => [1]
      (provided
        (#'instaskip.innkeeper-hosts-client/hosts-to-ids) => {"service" 1}))

(def path-mock-reponse {:id            1
                        :host-ids      [1 2]
                        :owned-by-team "theteam"
                        :created-by    "user"
                        :uri           "/uri"
                        :created-at    "2016"})

(facts "create-path"
       (fact "creates a path"

             (with-fake-routes
               {ik/paths-url
                (fn [_] {:status 200
                         :body   (json/clj->json path-mock-reponse)})
                ik/hosts-url
                (fn [_] {:status 200
                         :body   (json/clj->json [{:id 1 :name "host1.com"}
                                                  {:id 2 :name "host2.com"}])})}

               (create-path {:uri           "/uri"
                             :hosts         ["host1.com" "host2.com"]
                             :owned-by-team "team-1"})) => path-mock-reponse))
