(ns instaskip.impl.to-innkeeper
  (:require [instaskip.impl.from-eskip :as from-eskip :only [eskip->map]]
            [clojure.core.match :refer [match]]
            [clojure.string :refer [split]]))

(defn- ^{:testable true} split-hosts
  "Splits a regex of hosts into an array of hosts."
  [hosts-string]

  (let [trimmed-hosts (->> hosts-string
                           (re-find (re-pattern "\\/\\^\\((.*)\\)\\$\\/"))
                           peek)
        hosts-with-normalized-dot (.replace trimmed-hosts "[.]" ".")]
    (split hosts-with-normalized-dot #"[|]")))

(def ^{:private true :testable true} common-filters #{"fashionStore"})

(defn- ^{:testable true} filters-to-innkeeper
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

(defn- ^{:testable true} predicates-to-innkeeper
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
        (match [current-predicate]
               [{:name "Host" :args
                       [{:value value
                         :type  :regex}]}] (recur (rest old-predicates)
                                                  result-predicates
                                                  (split-hosts value)
                                                  uri)
               [{:name "Path" :args
                       [{:value value
                         :type  :string}]}] (recur (rest old-predicates)
                                                   result-predicates
                                                   hosts
                                                   value)
               :else (recur (rest old-predicates)
                            (conj result-predicates current-predicate)
                            hosts
                            uri))))))

(defn eskip-map-to-innkeeper
  "Transforms from an eskip map to an innkeeper map"
  [eskip-map]

  (let [innkeeper-predicates (predicates-to-innkeeper (-> eskip-map
                                                          :predicates
                                                          ))
        innkeeper-filters (filters-to-innkeeper (-> eskip-map
                                                    :filters))]
    {:route {:name                (eskip-map :name)
             :route               {:predicates (innkeeper-predicates :predicates)
                                   :filters    (innkeeper-filters :filters)
                                   :endpoint   (get eskip-map :endpoint "")}
             :uses-common-filters (innkeeper-filters :uses-common-filters)}
     :path  {:uri   (innkeeper-predicates :uri)
             :hosts (innkeeper-predicates :hosts)}}))
