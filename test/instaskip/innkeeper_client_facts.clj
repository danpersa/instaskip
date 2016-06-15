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

(def path-mock-response {:id            1
                         :host-ids      [1 2]
                         :owned-by-team "theteam"
                         :created-by    "user"
                         :uri           "/uri"
                         :created-at    "2016"})

(def paths-mock-response [path-mock-response])

(facts "get-paths"
       (fact "returns a list of paths"

             (with-fake-routes
               {ik/paths-url
                (fn [_] {:status 200
                         :body   (json/clj->json paths-mock-response)})}

               (get-paths)) => paths-mock-response))

(facts "get-path"
       (fact "returns a path"

             (with-fake-routes
               {(str ik/paths-url "/1")
                (fn [_] {:status 200
                         :body   (json/clj->json path-mock-response)})}

               (get-path 1)) => path-mock-response))

(facts "post-path"
       (fact "creates a path"

             (with-fake-routes
               {(str ik/paths-url)
                (fn [_] {:status 200
                         :body   (json/clj->json path-mock-response)})}

               (post-path {:uri           "/uri"
                           :host-ids      [1, 2]
                           :owned-by-team "team-1"})) => path-mock-response))

(def eskip-json-route {:predicates [{:name "pred"
                                     :args [{:value "value1"
                                             :type  "string"}]}]
                       :filters    [{:name "pred"
                                     :args [{:value "value1"
                                             :type  "string"}]}]})

(def route-mock-response {:id                  1
                          :name                "theroute"
                          :path-id             1
                          :uses-common-filters true
                          :created-by          "user"
                          :created-at          "2016"
                          :activate-at         "2016"
                          :description         "some desc"
                          :route               eskip-json-route})

(facts "post-route"
       (fact "creates a route"

             (with-fake-routes
               {(str ik/routes-url)
                (fn [_] {:status 200
                         :body   (json/clj->json route-mock-response)})}

               (post-route {:name                "theroute"
                            :route               eskip-json-route
                            :uses-common-filters true
                            :path-id             1
                            :activate-at         "2016"
                            :disable-at          "2016"
                            :description         "some desc"
                            }))
             => route-mock-response)

       (fact "creates a route withtout specifying the optional fields"

             (with-fake-routes
               {(str ik/routes-url)
                (fn [_] {:status 200
                         :body   (json/clj->json route-mock-response)})}

               (post-route {:name                "theroute"
                            :route               {}
                            :path-id             1
                            :uses-common-filters true
                            :activate-at         "2016"
                            :disable-at          "2016"
                            :description         "some desc"}))
             => route-mock-response))