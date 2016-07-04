(ns instaskip.route-with-path-process-facts
  (:require
    [instaskip.route-with-path-process :refer :all]
    [midje.sweet :refer :all]
    [midje.util :refer [testable-privates]]
    [clj-http.fake :refer [with-fake-routes]]
    [cats.monad.exception :as exc]
    [cats.core :as c]))

(testable-privates instaskip.route-with-path-process
                   hosts->ids
                   path-with-hosts->path-with-host-ids
                   host->host-id
                   hosts->ids)

(def hosts-to-host-ids {"hello-1.com" 1 "hello-2.com" 2})

(def innkeeper-config {:innkeeper-url "url" :oauth-token "token"})

(facts "host->host-id"

       (fact "returns success for an existing host"
             (host->host-id hosts-to-host-ids "hello-1.com") => (exc/success 1))

       (fact "returns failure for a non existing host"
             (ex-data (host->host-id hosts-to-host-ids "hello-3.com")) =>
             (ex-data (exc/failure (ex-info ""
                                            {:host "hello-3.com"})))))

(facts "hosts->ids"
       (fact "returns a success of host ids"
             (hosts->ids ["hello-1.com" "hello-2.com"] innkeeper-config)
             => (exc/success [1 2])

             (provided
               (#'instaskip.innkeeper-client/hosts-to-ids innkeeper-config)
               => hosts-to-host-ids))

       (fact "returns failure if there is a missing host id"
             (-> (hosts->ids ["hello-1.com" "hello-3.com"] innkeeper-config)
                 c/extract
                 ex-data) => {:host "hello-3.com"}

             (provided
               (#'instaskip.innkeeper-client/hosts-to-ids innkeeper-config)
               => hosts-to-host-ids)))


(facts "path-with-hosts->path-with-host-ids"
       (fact "returns a successful path if the host ids are present"

             (path-with-hosts->path-with-host-ids
               {:uri           "/uri"
                :hosts         ["hello-1.com" "hello-2.com"]
                :owned-by-team "team-1"}
               innkeeper-config) =>
             (exc/success {:uri           "/uri"
                           :host-ids      [1 2]
                           :owned-by-team "team-1"})

             (provided
               (#'instaskip.route-with-path-process/hosts->ids
                 ["hello-1.com" "hello-2.com"] innkeeper-config) =>
               (exc/success [1 2])))

       (fact "returns failure if there is a missing host"

             (-> (path-with-hosts->path-with-host-ids
                   {:uri           "/uri"
                    :hosts         ["hello-1.com" "hello-3.com"]
                    :owned-by-team "team-1"}
                   innkeeper-config)
                 c/extract
                 ex-data) =>
             {:host "host-3.com"}

             (provided
               (#'instaskip.route-with-path-process/hosts->ids
                 ["hello-1.com" "hello-3.com"] innkeeper-config) =>
               (exc/failure (ex-info "" {:host "host-3.com"})))))

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
                             :hosts         ["hello-1.com" "hello-2.com"]
                             :owned-by-team "theTeam"}})

(facts "route-with-path->innkeeper-route-with-path"
       (fact "transforms a route-with-path to an innkeeper-route-with-path"

             (route-with-path->innkeeper-route-with-path innkeeper-config route-with-path) =>

             (exc/success {:route (route-with-path :route)
                           :path  {:uri           "/path/example"
                                   :host-ids      [1 2]
                                   :owned-by-team "theTeam"}})

             (provided
               (#'instaskip.route-with-path-process/hosts->ids
                 ["hello-1.com" "hello-2.com"] innkeeper-config) =>
               (exc/success [1 2])))
       (fact "return a failure if the path has failed"

             (-> (route-with-path->innkeeper-route-with-path innkeeper-config route-with-path)
                 c/extract
                 ex-data) =>

             {:host "hello-2.com"}

             (provided
               (#'instaskip.route-with-path-process/hosts->ids
                 ["hello-1.com" "hello-2.com"] innkeeper-config) =>
               (exc/failure (ex-info "" {:host "hello-2.com"})))))