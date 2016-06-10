(ns instaskip.innkeeper-hosts-client-facts
  (:require
    [instaskip.innkeeper-hosts-client :refer :all]
    [midje.sweet :refer :all]
    [midje.util :refer [testable-privates]]
    [instaskip.json :refer [clj->json]]))

(testable-privates instaskip.innkeeper-hosts-client
                   hosts-response)

(facts "hosts-to-ids"
       (fact "returns a map from hosts to ids"
             (hosts-to-ids) => {
                                "host1.com" 1
                                "host2.com" 2}
             (provided
               (#'instaskip.innkeeper-hosts-client/hosts-response) =>
               {:body (clj->json [{:id 1 :name "host1.com"}
                                  {:id 2 :name "host2.com"}])})))

