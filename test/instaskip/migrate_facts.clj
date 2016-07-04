(ns instaskip.migrate-facts
  (:require [instaskip.migrate :refer :all]
            [midje.sweet :refer :all]
            [midje.util :refer [testable-privates]]
            [cats.monad.exception :as exc]
            [cats.core :as c]))

(testable-privates instaskip.migrate
                   team-names-in-dir
                   teams-with-eskip
                   teams-with-eskip-maps
                   path-without-star-predicate?
                   host-predicate?
                   filter-teams-with-eskip-maps
                   to-routes-with-paths
                   has-element?
                   to-innkeeper-routes-with-paths)

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

(facts "has-element?"
       (fact "return true if the collection has an element for which the predicate returns true"
             (has-element? [1 2 3] odd?) => true
             (has-element? [2 4 6] odd?) => false))

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
                                                         [{:name "Path" :args [{:value "/hello_*"}]}]}}
                                            {:team      "jarvis"
                                             :eskip-map {:predicates [{:name "Path"
                                                                       :args [{:value "/hello"
                                                                               :type  "string"}]}]}}]) =>
             [{:team      "team1"
               :eskip-map {:predicates
                           [{:name "Host"}
                            {:name "Path" :args [{:value "/hello"}]}]}}]))

(facts "to-routes-with-paths"
       (fact "transforms to routes-with-paths"
             (to-routes-with-paths [{:team      "team1"
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

(def innkeeper-config {:innkeeper-url "url" :oauth-token "token"})

(defn route-with-path [name hosts] {:path  {:hosts         hosts
                                            :owned-by-team "team1"
                                            :uri           "/hello"}
                                    :route {:name                name
                                            :predicates          []
                                            :filters             []
                                            :endpoint            ""
                                            :uses-common-filters false}})

(def route-with-path-1 (route-with-path "theroute1" ["host1.com" "host2.com"]))
(def route-with-path-2 (route-with-path "theroute2" ["host2.com" "host3.com"]))

(def routes-with-paths [route-with-path-1 route-with-path-2])

(def innkeeper-route-with-path {:path  {:host-ids      [1 2]
                                        :owned-by-team "team1"
                                        :uri           "/hello"}
                                :route {:name                "theroute"
                                        :predicates          []
                                        :filters             []
                                        :endpoint            ""
                                        :uses-common-filters false}})

(facts "to-innkeeper-routes-with-paths"
       (fact "returns a Success of innkeeper routes"
             (to-innkeeper-routes-with-paths routes-with-paths
                                             innkeeper-config) => (exc/success [innkeeper-route-with-path
                                                                             innkeeper-route-with-path])

             (provided
               (#'instaskip.route-with-path-process/route-with-path->innkeeper-route-with-path
                 innkeeper-config
                 route-with-path-1) => (exc/success innkeeper-route-with-path)
               (#'instaskip.route-with-path-process/route-with-path->innkeeper-route-with-path
                 innkeeper-config
                 route-with-path-2) => (exc/success innkeeper-route-with-path)))

       (fact "returns a Failure in case something fails"
             (-> (to-innkeeper-routes-with-paths routes-with-paths
                                                 innkeeper-config)
                 c/extract
                 ex-data) => {:host "host3.com"}

             (provided
               (#'instaskip.route-with-path-process/route-with-path->innkeeper-route-with-path
                 innkeeper-config
                 route-with-path-1) => (exc/success innkeeper-route-with-path)
               (#'instaskip.route-with-path-process/route-with-path->innkeeper-route-with-path
                 innkeeper-config
                 route-with-path-2) => (exc/failure (ex-info "" {:host "host3.com"})))))
