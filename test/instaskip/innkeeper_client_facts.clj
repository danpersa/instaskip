(ns instaskip.innkeeper-client-facts
  (:require
    [instaskip.innkeeper-client :refer :all]
    [midje.sweet :refer :all]
    [midje.util :refer [testable-privates]]
    [clj-http.fake :refer [with-fake-routes]]
    [instaskip.innkeeper-client :as ik]
    [instaskip.json :as json]))


(facts "get-hosts"
       (fact "returns a map of hosts"

             (with-fake-routes
               {ik/hosts-url
                (fn [_] {:status 200
                         :body   (json/clj->json
                                   [{:id 1 :name "host1.com"}
                                    {:id 2 :name "host2.com"}])})}

               (get-hosts) => [{:id 1 :name "host1.com"}
                               {:id 2 :name "host2.com"}])))

(def path-mock-reponse {:id            1
                        :host-ids      [1 2]
                        :owned-by-team "theteam"
                        :created-by    "user"
                        :uri           "/uri"
                        :created-at    "2016"})

(def paths-mock-reposnse [path-mock-reponse])

(facts "get-paths"
       (fact "returns a list of paths"

             (with-fake-routes
               {ik/paths-url
                (fn [_] {:status 200
                         :body   (json/clj->json paths-mock-reposnse)})}

               (get-paths)) => paths-mock-reposnse))
