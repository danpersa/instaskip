(ns instaskip.route-with-path-process-facts
  (:require
    [instaskip.route-with-path-process :refer :all]
    [midje.sweet :refer :all]
    [midje.util :refer [testable-privates]]
    [clj-http.fake :refer [with-fake-routes]]))

(testable-privates instaskip.route-with-path-process
                   transform-hosts-to-ids
                   path-with-hosts->path-with-host-ids)

(def innkeeper-config {:innkeeper-url "url" :oauth-token "token"})

(facts "path-with-hosts->path-with-host-ids"
       (fact "transforms path with hosts to path with host ids"

             (path-with-hosts->path-with-host-ids
               {:uri           "/uri"
                :hosts         ["service.com"]
                :owned-by-team "team-1"}
               innkeeper-config) =>
             {:uri           "/uri"
              :host-ids      [1]
              :owned-by-team "team-1"}

             (provided
               (#'instaskip.route-with-path-process/transform-hosts-to-ids
                 ["service.com"] innkeeper-config) => [1])))

(facts "transform-hosts-to-ids"
       (fact "transforms a list of hosts to a list of ids"

             (transform-hosts-to-ids ["service"] innkeeper-config) => [1]

             (provided
               (#'instaskip.innkeeper-client/hosts-to-ids innkeeper-config) =>
               {"service" 1})))

(def route-with-path {:route
                            {:name                "theRoute",
                             :predicates          [{:name "Method"
                                                    :args [{:type "string" :value "GET"}]}]
                             :filters             [{:name "filter1"
                                                    :args []}
                                                   {:name "filter2"
                                                    :args [{:type "string" :value "value"}]}]
                             :endpoint            ""
                             :uses-common-filters true}
                      :path {:uri           "/path/example"
                             :hosts         ["host1.com" "host2.com"]
                             :owned-by-team "theTeam"}})

(facts "route-with-path->innkeeper-route-with-path"
       (fact "transforms a route-with-path to an innkeeper-route-with-path"

             (route-with-path->innkeeper-route-with-path innkeeper-config route-with-path) =>
             {:route (route-with-path :route)
              :path  {:uri           "/path/example"
                      :host-ids      [1 2]
                      :owned-by-team "theTeam"}}

             (provided
               (#'instaskip.route-with-path-process/transform-hosts-to-ids
                 ["host1.com" "host2.com"]
                 innkeeper-config) => [1 2])))