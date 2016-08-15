(ns instaskip.impl.eskip-map-process-facts
  (:require
    [instaskip.impl.eskip-map-process :refer :all]
    [midje.sweet :refer :all]
    [midje.util :refer [testable-privates]]))

(testable-privates instaskip.impl.eskip-map-process
                   predicates-to-innkeeper
                   filters-to-innkeeper
                   split-hosts-dispatch
                   trim-hosts
                   combine-prefix-mid-suffix)

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

(facts "trim-hosts"
       (trim-hosts "/^(host1[.]com|host2[.]com|host3[.]com)$/") => "(host1[.]com|host2[.]com|host3[.]com)"
       (trim-hosts "/^(www|m)[.]host1[.](com|de)$/") => "(www|m)[.]host1[.](com|de)"
       (trim-hosts "/^(www|m)[.]host1[.]com$/") => "(www|m)[.]host1[.]com"
       (trim-hosts "/^(host1[.]com|host2[.]com|host3[.]com)$/") => "(host1[.]com|host2[.]com|host3[.]com)")

(facts "split-hosts-dispatch"
       (split-hosts-dispatch "/^(host1[.]com|host2[.]com|host3[.]com)$/") => :list-of-hosts
       (split-hosts-dispatch "/^(www|m)[.]host1[.](com|de)$/") => :prefix-and-suffix
       (split-hosts-dispatch "/^(www|m)[.]host1[.]com$/") => :prefix
       (split-hosts-dispatch "/^m[.]host1[.](com|de)$/") => :suffix)

(fact "combine-prefix-mid-suffix"
      (combine-prefix-mid-suffix "m|www" "[.]host[.]" "de|com") => ["m.host.de" "m.host.com" "www.host.de" "www.host.com"]
      (combine-prefix-mid-suffix "m|www" "[.]host[.]com" "") => ["m.host.com" "www.host.com"]
      (combine-prefix-mid-suffix "" "m[.]host[.]" "de|com") => ["m.host.de" "m.host.com"])

(facts "split-hosts"
       (fact "splits a regex string into more hosts"
             (split-hosts "/^(host1[.]com|host2[.]com|host3[.]com)$/") =>
             ["host1.com" "host2.com" "host3.com"]

             (split-hosts "/^(www|m)[.]host1[.](com|de)$/") =>
             ["www.host1.com" "www.host1.de" "m.host1.com" "m.host1.de"]

             (split-hosts "/^(www|m)[.]host1[.]com$/") =>
             ["www.host1.com" "m.host1.com"]

             (split-hosts "/^m[.]host1[.](com|de)$/") =>
             ["m.host1.com" "m.host1.de"]))

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
                              :args [{:value "/^(host1[.]com|host2[.]com)$/",
                                      :type  "regex"}]}],
                :filters    [{:name "fashionStore", :args []}],
                :endpoint   "https://endpoint.com"}) =>

             {:path
                     {:hosts         ["host1.com" "host2.com"]
                      :uri           "/api/wishlist"
                      :owned-by-team "theTeam"}
              :route {:name                "aladdin_genieWishlistItemsApi"
                      :predicates          []
                      :filters             []
                      :endpoint            "https://endpoint.com"
                      :uses-common-filters true}}))
