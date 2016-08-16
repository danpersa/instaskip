(ns instaskip.impl.eskip-map-process
  (:require [instaskip.impl.from-eskip :as from-eskip :only [eskip->map]]
            [clojure.core.match :as m]
            [clojure.string :refer [split]]
            [clojure.spec :as s]
            [clojure.spec.test :as st]
            [clojure.tools.logging :as log]))

(defn- trim-hosts [hosts-string]
  (if-some [trimmed-hosts (re-find (re-pattern "\\/\\^(.+)\\$\\/") hosts-string)]
    (peek trimmed-hosts)
    hosts-string))

(defn- split-hosts-cond [regex hosts-string]
  (not-empty
    (->> hosts-string
         (re-find (re-pattern regex)))))

(def ^:private case-to-regex
  {:prefix-and-suffix "\\((.*|.*)\\)(.+)\\((.+)\\)"
   :double-prefix     "\\((.+)\\)[-]\\((.+)\\)(.+)"
   :prefix            "\\((.+)\\)(.+)"
   :suffix            "(.+)\\((.+)\\)"
   :list-of-hosts     "\\((.+)\\)"})

(defn- split-hosts-dispatch [hosts-string]

  (let [trimmed-hosts (trim-hosts hosts-string)]
    (cond
      (split-hosts-cond (case-to-regex :double-prefix) trimmed-hosts) :double-prefix
      (split-hosts-cond (case-to-regex :prefix-and-suffix) trimmed-hosts) :prefix-and-suffix
      (split-hosts-cond (case-to-regex :prefix) trimmed-hosts) :prefix
      (split-hosts-cond (case-to-regex :suffix) trimmed-hosts) :suffix
      (split-hosts-cond (case-to-regex :list-of-hosts) trimmed-hosts) :list-of-hosts)))

(defn- combine-parts [how parts-1 parts-2 parts-3]
  (let [hosts (for [part-1 (split parts-1 #"[|]")
                    part-2 (split parts-2 #"[|]")
                    part-3 (split parts-3 #"[|]")]
                (how part-1 part-2 part-3))
        hosts-with-normalized-dot (map #(.replace % "[.]" ".") hosts)]
    hosts-with-normalized-dot))

(defn- combine-prefix-mid-suffix [parts-1 parts-2 parts-3]
  (combine-parts #(str %1 %3 %2) parts-1 parts-2 parts-3))

(defn- combine-prefixes-mid [parts-1 parts-2 parts-3]
  (combine-parts #(str %1 "-" %2 %3) parts-1 parts-2 parts-3))

(defn- split-by-regex [case trimmed-hosts]
  (->> trimmed-hosts
       (re-find (re-pattern (case-to-regex case)))))

(defmulti split-hosts split-hosts-dispatch)

(defmethod split-hosts :list-of-hosts [hosts-string]
  (let [trimmed-hosts (trim-hosts hosts-string)
        hosts (->> trimmed-hosts
                   (split-by-regex :list-of-hosts)
                   peek)
        hosts-with-normalized-dot (.replace hosts "[.]" ".")]
    (split hosts-with-normalized-dot #"[|]")))

(defmethod split-hosts :prefix-and-suffix [hosts-string]
  (log/debug "prefix and suffix")
  (let [trimmed-hosts (trim-hosts hosts-string)
        [_ prefixes middle suffixes]
        (->> trimmed-hosts
             (split-by-regex :prefix-and-suffix))]
    (combine-prefix-mid-suffix prefixes suffixes middle)))

(defmethod split-hosts :prefix [hosts-string]
  (log/debug "prefix")
  (let [trimmed-hosts (trim-hosts hosts-string)
        [_ prefixes middle]
        (->> trimmed-hosts
             (split-by-regex :prefix))]
    (combine-prefix-mid-suffix prefixes "" middle)))

(defmethod split-hosts :suffix [hosts-string]
  (log/debug "suffix")
  (let [trimmed-hosts (trim-hosts hosts-string)
        [_ middle suffixes]
        (->> trimmed-hosts
             (split-by-regex :suffix))]
    (combine-prefix-mid-suffix "" suffixes middle)))

(defmethod split-hosts :double-prefix [hosts-string]
  (log/debug "double prefix")
  (let [trimmed-hosts (trim-hosts hosts-string)
        [_ prefixes-1 prefixes-2 middle]
        (->> trimmed-hosts
             (split-by-regex :double-prefix))]
    (combine-prefixes-mid prefixes-1 prefixes-2 middle)))

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

(st/instrument `eskip-map->route-with-path)
