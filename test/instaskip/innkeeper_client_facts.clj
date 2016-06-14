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
