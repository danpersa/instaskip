(ns instaskip.innkeeper-client-facts
  (:require
    [instaskip.innkeeper-client :refer :all]
    [midje.sweet :refer :all]
    [midje.util :refer [testable-privates]]
    [clj-http.fake :refer [with-fake-routes]]
    [instaskip.innkeeper-client :as ik]
    [instaskip.json :as json]))

(def innkeeper-url "http://localhost:9080")
(def innkeeper-admin-config {:innkeeper-url innkeeper-url
                             :oauth-token   ik/admin-token})

(def innkeeper-read-config {:innkeeper-url innkeeper-url
                            :oauth-token   ik/admin-token})

(facts "get-hosts"
       (fact "returns a map of hosts"

             (with-fake-routes
               {(hosts-url innkeeper-url)
                (fn [_] {:status 200
                         :body   (json/clj->json
                                   [{:id 1 :name "host1.com"}
                                    {:id 2 :name "host2.com"}])})}

               (get-hosts innkeeper-read-config) => [{:id 1 :name "host1.com"}
                                                     {:id 2 :name "host2.com"}])))

(facts "hosts-to-ids"
       (fact "returns a map from hosts to ids"

             (with-fake-routes
               {(ik/hosts-url innkeeper-url)
                (fn [_] {:status 200
                         :body   (json/clj->json [{:id 1 :name "host1.com"}
                                                  {:id 2 :name "host2.com"}])})}

               (hosts-to-ids innkeeper-read-config) => {"host1.com" 1
                                                        "host2.com" 2})))

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
               {(ik/paths-url innkeeper-url)
                (fn [_] {:status 200
                         :body   (json/clj->json paths-mock-response)})}

               (get-paths innkeeper-read-config)) => paths-mock-response))

(fact "path-uris-to-paths"
      (fact "returs a map from path-uris to paths"
            (with-fake-routes
              {(ik/paths-url innkeeper-url)
               (fn [_] {:status 200
                        :body   (json/clj->json paths-mock-response)})}

              (path-uris-to-paths innkeeper-read-config) => {"/uri" path-mock-response})))

(facts "get-path"
       (fact "returns a path"

             (with-fake-routes
               {(str (ik/paths-url innkeeper-url) "/1")
                (fn [_] {:status 200
                         :body   (json/clj->json path-mock-response)})}

               (get-path 1
                         innkeeper-read-config)) => path-mock-response))

(facts "post-path"
       (fact "creates a path"

             (with-fake-routes
               {(ik/paths-url innkeeper-url)
                (fn [_] {:status 200
                         :body   (json/clj->json path-mock-response)})}

               (post-path {:uri           "/uri"
                           :host-ids      [1]
                           :owned-by-team "team-1"}
                          innkeeper-admin-config)) => path-mock-response))

(facts "patch-path"
       (fact "patches a path"

             (with-fake-routes
               {(str (ik/paths-url innkeeper-url) "/1")
                (fn [_] {:status 200
                         :body   (json/clj->json path-mock-response)})}

               (patch-path 1
                           {:host-ids      [1 2]
                            :owned-by-team "team-1"}
                           innkeeper-admin-config)) => path-mock-response))

(def route-mock-response {:id                  1
                          :name                "theroute"
                          :path-id             1
                          :uses-common-filters true
                          :created-by          "user"
                          :created-at          "2016"
                          :activate-at         "2016"
                          :description         "some desc"
                          :predicates          [{:name "pred"
                                                 :args [{:value "value1"
                                                         :type  "string"}]}]
                          :filters             [{:name "pred"
                                                 :args [{:value "value1"
                                                         :type  "string"}]}]})


(facts "post-route"
       (fact "creates a route"

             (with-fake-routes
               {(ik/routes-url innkeeper-url)
                (fn [_] {:status 200
                         :body   (json/clj->json route-mock-response)})}

               (post-route {:name                "theroute"
                            :predicates          [{:name "pred"
                                                   :args [{:value "value1"
                                                           :type  "string"}]}]
                            :filters             [{:name "pred"
                                                   :args [{:value "value1"
                                                           :type  "string"}]}]
                            :uses-common-filters true
                            :path-id             1
                            :activate-at         "2016"
                            :disable-at          "2016"
                            :description         "some desc"
                            }
                           innkeeper-admin-config))
             => route-mock-response)

       (fact "creates a route withtout specifying the optional fields"

             (with-fake-routes
               {(ik/routes-url innkeeper-url)
                (fn [_] {:status 200
                         :body   (json/clj->json route-mock-response)})}

               (post-route {:name                "theroute"
                            :route               {}
                            :path-id             1
                            :uses-common-filters true
                            :activate-at         "2016"
                            :disable-at          "2016"
                            :description         "some desc"}
                           innkeeper-admin-config))
             => route-mock-response))

(facts "get-route"
       (fact "gets the route with the specified id"
             (with-fake-routes
               {(str (ik/routes-url innkeeper-url) "/" 1)
                (fn [_] {:status 200
                         :body   (json/clj->json route-mock-response)})}

               (get-route 1 innkeeper-admin-config))))

(facts "get-routes-by-name"
       (fact "gets the route with the specified id"
             (with-fake-routes
               {(str (ik/routes-url innkeeper-url) "?name=theroute")
                (fn [_] {:status 200
                         :body   (json/clj->json [route-mock-response])})}

               (get-routes-by-name "theroute" innkeeper-admin-config))))
