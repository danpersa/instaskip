(ns instaskip.impl.eskip-map-process
  (:require [instaskip.impl.from-eskip :as from-eskip :only [eskip->map]]
            [clojure.core.match :as m]
            [clojure.string :refer [split]]
            [clojure.spec :as s]))

(defn- split-hosts
  "Splits a regex of hosts into an array of hosts."
  [hosts-string]

  (let [trimmed-hosts (->> hosts-string
                           (re-find (re-pattern "\\/\\^\\((.*)\\)\\$\\/"))
                           peek)
        hosts-with-normalized-dot (.replace trimmed-hosts "[.]" ".")]
    (split hosts-with-normalized-dot #"[|]")))

(def ^{:private true} common-filters #{"fashionStore"})

(defn- filters-to-innkeeper
  "Transforms a list of filters to a map containg the fact that the route uses the common filters.
   Removes the common filters from the initial list"
  [filters]

  (loop [old-filters filters
         result-filters []
         uses-common-filters false]
    (if (empty? old-filters)
      {:uses-common-filters uses-common-filters
       :filters             result-filters}
      (let [current-filter (first old-filters)
            current-filter-is-common-filter (contains? common-filters (current-filter :name))
            new-filters (if current-filter-is-common-filter
                          result-filters
                          (conj result-filters current-filter))
            new-use-common-filters (or current-filter-is-common-filter uses-common-filters)]

        (recur (rest old-filters)
               new-filters
               new-use-common-filters)))))

(defn- predicates-to-innkeeper
  "Transforms a list of predicates to a map containing innkeeper predicates, uris and hosts.

   It returns a map with the format:
   { :hosts [\"host1\", \"host2\"]
     :uri   \"/uri\"
     :predicates [ ... ] }"
  [predicates]

  (loop [old-predicates predicates
         result-predicates []
         hosts []
         uri nil]
    (if (empty? old-predicates)
      {:hosts      hosts
       :uri        uri
       :predicates result-predicates}
      (let [current-predicate (first old-predicates)]
        (m/match [current-predicate]
                 [{:name "Host" :args
                         [{:value value
                           :type  "regex"}]}] (recur (rest old-predicates)
                                                     result-predicates
                                                     (split-hosts value)
                                                     uri)
                 [{:name "Path" :args
                         [{:value value
                           :type  "string"}]}] (recur (rest old-predicates)
                                                      result-predicates
                                                      hosts
                                                      value)
                 :else (recur (rest old-predicates)
                              (conj result-predicates current-predicate)
                              hosts
                              uri))))))

(s/def :ti/eskip-map (s/keys :req-un
                             [:ik/name
                              :ik/predicates
                              :ik/filters
                              :ik/endpoint]))

(s/def :ti/route (s/keys :req-un [:ik/name
                                  :ik/predicates
                                  :ik/filters
                                  :ik/endpoint
                                  :ik/uses-common-filters]))

(s/def :ti/host string?)

; make sure we have at least one host
(s/def :ti/hosts (s/+ :ti/host))

(s/def :ti/path (s/keys :req-un [:ik/uri :ik/owned-by-team :ti/hosts]))

(s/def :ti/route-with-path (s/keys :req-un [:ti/route :ti/path]))

(s/fdef eskip-map->route-with-path
        :args (s/cat :team-name string? :eskip-map :ti/eskip-map)
        :ret :ti/route-with-path)

(defn eskip-map->route-with-path
  "Transforms from an eskip map to an innkeeper map"
  [team-name eskip-map]

  (let [innkeeper-predicates (predicates-to-innkeeper (-> eskip-map
                                                          :predicates))
        innkeeper-filters (filters-to-innkeeper (-> eskip-map
                                                    :filters))]
    {:route {:name                (eskip-map :name)
             :predicates          (innkeeper-predicates :predicates)
             :filters             (innkeeper-filters :filters)
             :endpoint            (get eskip-map :endpoint "")
             :uses-common-filters (innkeeper-filters :uses-common-filters)}
     :path  {:uri           (innkeeper-predicates :uri)
             :hosts         (innkeeper-predicates :hosts)
             :owned-by-team team-name}}))

(s/instrument #'eskip-map->route-with-path)
