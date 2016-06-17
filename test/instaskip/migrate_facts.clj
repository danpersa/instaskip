(ns instaskip.migrate-facts
  (:require [instaskip.migrate :refer :all]
            [midje.sweet :refer :all]
            [midje.util :refer [testable-privates]]))

(testable-privates instaskip.migrate
                   team-names-in-dir
                   teams-with-eskip
                   teams-with-eskip-maps
                   path-without-star-predicate?
                   host-predicate?
                   filter-teams-with-eskip-maps
                   routes-with-paths)

(facts "team-names-in-dir"
       (fact "returns the teams"
             (team-names-in-dir "./eskip-routes") => ["team-1" "team-2"]))

(facts "teams-with-eskip"
       (fact "returns the teams with eskip markup"
             (teams-with-eskip ["team-1" "team-2"] "./eskip-routes") =>
             [{:team "team-1" :eskip "eskip-1"}
              {:team "team-2" :eskip "eskip-2"}]))

(def eskip-1
  (str "team1_WishlistApi: Path(\"/api/wishlist\") && Host(/^(host1[.]domain[.]com|host2[.]domain[.]com)$/)"
       "  -> fashionStore()"
       "  -> modPath(\".*\", \"/team-1/wishlist\")"
       "  -> \"https://wishlist.team1-staging.domain.com\";\n"
       "team1_OtherApi: Path(\"/api/other\") && Host(/^(host1[.]domain[.]com|host2[.]domain[.]com)$/)"
       "  -> fashionStore()"
       "  -> \"https://other.team1-staging.domain.com\";"))

(def eskip-2
  (str "team1_WishlistApi: Path(\"/api/wishlist\") && Host(/^(host1[.]domain[.]com|host2[.]domain[.]com)$/)"
       "  -> \"https://wishlist.team1-staging.domain.com\";"))


(def eskip-map-1 {:name       "team1_WishlistApi"
                  :predicates [{:name "Path"
                                :args [{:type  "string"
                                        :value "/api/wishlist"}]}
                               {:name "Host"
                                :args [{:type  "regex"
                                        :value "/^(host1[.]domain[.]com|host2[.]domain[.]com)$/"}]}]
                  :filters    [{:name "fashionStore"
                                :args []}
                               {:name "modPath"
                                :args [{:type  "string"
                                        :value ".*"}
                                       {:type  "string"
                                        :value "/team-1/wishlist"}]}]
                  :endpoint   "https://wishlist.team1-staging.domain.com"})

(def eskip-map-2 {:name       "team1_OtherApi"
                  :predicates [{:name "Path"
                                :args [{:type  "string"
                                        :value "/api/other"}]}
                               {:args [{:type  "regex"
                                        :value "/^(host1[.]domain[.]com|host2[.]domain[.]com)$/"}]
                                :name "Host"}]

                  :filters    [{:name "fashionStore"
                                :args []}]
                  :endpoint   "https://other.team1-staging.domain.com"})

(def eskip-map-3 {:name       "team1_WishlistApi"
                  :predicates [{:name "Path"
                                :args [{:type  "string"
                                        :value "/api/wishlist"}]}
                               {:name "Host"
                                :args [{:type  "regex"
                                        :value "/^(host1[.]domain[.]com|host2[.]domain[.]com)$/"}]}]
                  :filters    []
                  :endpoint   "https://wishlist.team1-staging.domain.com"})

(facts "teams-with-eskip-maps"
       (fact "returns the teams with eskip maps"
             (teams-with-eskip-maps
               [{:team  "team-1"
                 :eskip eskip-1}
                {:team  "team-2"
                 :eskip eskip-2}]) => [{:team      "team-1"
                                        :eskip-map eskip-map-1}
                                       {:team      "team-1"
                                        :eskip-map eskip-map-2}
                                       {:team      "team-2"
                                        :eskip-map eskip-map-3}]))
(facts "path-without-star-predicate"
       (fact "accepts a Path eskip predicate"
             (path-without-star-predicate? {:name "Path" :args [{:value "/hello"}]}) => true)
       (fact "rejects a Path eskip predicate with a *"
             (path-without-star-predicate? {:name "Path" :args [{:value "/hello/_*"}]}) => false)
       (fact "rejects a Host eskip predicate"
             (path-without-star-predicate? {:name "Host" :args [{:value "/hello/_*"}]}) => false))

(facts "host-present-predicate"
       (fact "accepts a Host eskip predicate"
             (host-predicate? {:name "Host"}))
       (fact "rejects a Path eskip predicate"
             (host-predicate? {:name "Path"})))

(facts "filter-teams-with-eskip-maps"
       (fact "filters the correct eskip predicates"
             (filter-teams-with-eskip-maps [{:team      "team1"
                                             :eskip-map {:predicates
                                                         [{:name "Host"}
                                                          {:name "Path" :args [{:value "/hello"}]}]}}
                                            {:team      "team2"
                                             ; Path predicate with a *
                                             :eskip-map {:predicates
                                                         [{:name "Host"}
                                                          {:name "Path" :args [{:value "/hello_*"}]}]}}
                                            {:team      "team3"
                                             ; missing Host predicate
                                             :eskip-map {:predicates
                                                         [{:name "Path" :args [{:value "/hello_*"}]}]}}]) =>
             [{:team      "team1"
               :eskip-map {:predicates
                           [{:name "Host"}
                            {:name "Path" :args [{:value "/hello"}]}]}}]))

(facts "routes-with-paths"
       (fact "transforms to routes-with-paths"
             (routes-with-paths [{:team      "team1"
                                  :eskip-map {:name       "theroute"
                                              :predicates [{:name "Host"
                                                            :args [{:type "regex" :value "/^(host1.com|host2.com)$/"}]}
                                                           {:name "Path"
                                                            :args [{:type "string" :value "/hello"}]}]
                                              :filters    []
                                              :endpoint   ""
                                              }}]) =>
             [{:path  {:hosts         ["host1.com" "host2.com"]
                       :owned-by-team "team1"
                       :uri           "/hello"}
               :route {:name                "theroute"
                       :predicates          []
                       :filters             []
                       :endpoint            ""
                       :uses-common-filters false}}]))