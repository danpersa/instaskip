(ns instaskip.impl.eskip-map-process-facts
  (:require
    [instaskip.impl.eskip-map-process :refer :all]
    [midje.sweet :refer :all]
    [midje.util :refer [testable-privates]]))

(testable-privates instaskip.impl.eskip-map-process
                   predicates-to-innkeeper
                   filters-to-innkeeper
                   split-hosts)

(facts "predicates-to-innkeeper"
       (fact "transforms from predicates to map"
             (predicates-to-innkeeper [{:name "Host"
                                        :args [{:value "/^(host1.com|host2.com)$/"
                                                :type  "regex"}]}
                                       {:name "Path" :args [{:value "/path/example"
                                                             :type  "string"}]}
                                       {:name "Method" :args [{:value "GET"
                                                               :type  "string"}]}]) =>
             {:hosts      ["host1.com" "host2.com"]
              :uri        "/path/example"
              :predicates [{:name "Method" :args [{:value "GET"
                                                   :type  "string"}]}]}))

(facts "filters-to-innkeeper"
       (fact "transforms from filters to map with common filters"
             (filters-to-innkeeper [{:name "filter1" :args []}
                                    {:name "fashionStore" :args []}
                                    {:name "filter2" :args [{:value "value"
                                                             :type  "string"}]}]) =>
             {:uses-common-filters true
              :filters             [{:name "filter1" :args []}
                                    {:name "filter2" :args [{:value "value"
                                                             :type  "string"}]}]})

       (fact "transforms from filters to map without common filters"
             (filters-to-innkeeper [{:name "filter1" :args []}
                                    {:name "filter2" :args [{:value "value"
                                                             :type  "string"}]}]) =>
             {:uses-common-filters false
              :filters             [{:name "filter1" :args []}
                                    {:name "filter2" :args [{:value "value"
                                                             :type  "string"}]}]}))

(facts "split-hosts"
       (fact "splits a regex string into more hosts"
             (split-hosts "/^(host1[.]com|host2[.]com|host3[.]com)$/") =>
             ["host1.com" "host2.com" "host3.com"]))

(facts "eskip-map->route-with-path"
       (fact "transforms from eskip map to innkeeper map"
             (eskip-map->route-with-path
               "theTeam"
               {:name       "theRoute"
                :predicates [{:name "Host"
                              :args [{:value "/^(host1[.]com|host2[.]com)$/"
                                      :type  "regex"}]}
                             {:name "Path" :args [{:value "/path/example"
                                                   :type  "string"}]}
                             {:name "Method" :args [{:value "GET"
                                                     :type  "string"}]}]
                :filters    [{:name "filter1" :args []}
                             {:name "fashionStore" :args []}
                             {:name "filter2" :args [{:value "value"
                                                      :type  "string"}]}]
                :endpoint   ""}) =>

             {:route
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

       (fact "transforms from eskip to innkeeper a real route"
             (eskip-map->route-with-path
               "theTeam"
               {:name       "aladdin_genieWishlistItemsApi",
                :predicates [{:name "Path", :args [{:value "/api/wishlist", :type "string"}]}
                             {:name "Host",
                              :args [{:value "/^(m-it[.]release[.]zalando[.]net|m-pl[.]release[.]zalando[.]net)$/",
                                      :type  "regex"}]}],
                :filters    [{:name "fashionStore", :args []}],
                :endpoint   "https://genie.aladdin-staging.zalan.do"}) =>

             {:path
                     {:hosts         ["m-it.release.zalando.net" "m-pl.release.zalando.net"]
                      :uri           "/api/wishlist"
                      :owned-by-team "theTeam"}
              :route {:name                "aladdin_genieWishlistItemsApi"
                      :predicates          []
                      :filters             []
                      :endpoint            "https://genie.aladdin-staging.zalan.do"
                      :uses-common-filters true}}))
