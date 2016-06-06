(ns instaskip.impl.to-innkeeper-facts
  (:require
    [instaskip.impl.to-innkeeper :refer :all]
    [midje.sweet :refer :all]
    [midje.util :refer [expose-testables]]))

(expose-testables instaskip.impl.to-innkeeper)

(facts "predicates-to-innkeeper"
       (fact "transforms from predicates to map"
             (predicates-to-innkeeper [{:name "Host"
                                        :args [{:value "/^(host1.com|host2.com)$/"
                                                :type  :regex}]}
                                       {:name "Path" :args [{:value "/path/example"
                                                             :type  :string}]}
                                       {:name "Method" :args [{:value "GET"
                                                               :type  :string}]}]) =>
             {:hosts      ["host1.com" "host2.com"]
              :uri        "/path/example"
              :predicates [{:name "Method" :args [{:value "GET"
                                                   :type  :string}]}]}
             ))

(facts "filters-to-innkeeper"
       (fact "transforms from filters to map with common filters"
             (filters-to-innkeeper [{:name "filter1" :args []}
                                    {:name "fashionStore" :args []}
                                    {:name "filter2" :args [{:value "value"
                                                             :type  :string}]}]) =>
             {:uses-common-filters true
              :filters             [{:name "filter1" :args []}
                                    {:name "filter2" :args [{:value "value"
                                                             :type  :string}]}]})

       (fact "transforms from filters to map without common filters"
             (filters-to-innkeeper [{:name "filter1" :args []}
                                    {:name "filter2" :args [{:value "value"
                                                             :type  :string}]}]) =>
             {:uses-common-filters false
              :filters             [{:name "filter1" :args []}
                                    {:name "filter2" :args [{:value "value"
                                                             :type  :string}]}]}))

(facts "split-hosts"
       (fact "splits a regex string into more hosts"
             (split-hosts "/^(host1[.]com|host2[.]com|host3[.]com)$/") =>
             ["host1[.]com" "host2[.]com" "host3[.]com"]))

(facts "eskip-map-to-innkeeper"
       (fact "transforms from eskip map to innkeeper map"
             (eskip-map-to-innkeeper
               {:name                "theRoute"
                :route               {:predicates [{:name "Host"
                                                    :args [{:value "/^(host1.com|host2.com)$/"
                                                            :type  :regex}]}
                                                   {:name "Path" :args [{:value "/path/example"
                                                                         :type  :string}]}
                                                   {:name "Method" :args [{:value "GET"
                                                                           :type  :string}]}]
                                      :filters    [{:name "filter1" :args []}
                                                   {:name "fashionStore" :args []}
                                                   {:name "filter2" :args [{:value "value"
                                                                            :type  :string}]}]
                                      :endpoint   ""}
                :uses-common-filters true}) =>

             {:route
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
                     :hosts ["host1.com" "host2.com"]
                     }}))