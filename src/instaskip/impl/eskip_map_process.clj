(ns instaskip.impl.eskip-map-process
  (:require [instaskip.impl.from-eskip :as from-eskip :only [eskip->map]]
            [clojure.core.match :as m]
            [clojure.string :refer [split]]
            [clojure.spec :as s]
            [clojure.spec.test :as st]))

(defn- trim-hosts [hosts-string]
  (if-some [trimmed-hosts (re-find (re-pattern "\\/\\^(.+)\\$\\/") hosts-string)]
    (peek trimmed-hosts)
    hosts-string))

(trim-hosts "/^(www|m)[.]host1[.]com$/")

(defn- split-hosts-cond [regex hosts-string]
  (not-empty
    (->> hosts-string
         (re-find (re-pattern regex)))))

(def ^:private case-to-regex
  {:prefix-and-suffix "\\((.+)\\)(.+)\\((.+)\\)"
   :prefix            "\\((.+)\\)(.+)"
   :suffix            "(.+)\\((.+)\\)"
   :list-of-hosts     "\\((.+)\\)"})


(split-hosts-cond (case-to-regex :prefix) "(www|m)[.]host1[.]com")
(split-hosts-cond (case-to-regex :suffix) "m[.]host1[.](com|de)")

(defn- split-hosts-dispatch [hosts-string]

  (let [trimmed-hosts (trim-hosts hosts-string)]
    (cond
      (split-hosts-cond (case-to-regex :prefix-and-suffix) trimmed-hosts) :prefix-and-suffix
      (split-hosts-cond (case-to-regex :prefix) trimmed-hosts) :prefix
      (split-hosts-cond (case-to-regex :suffix) trimmed-hosts) :suffix
      (split-hosts-cond (case-to-regex :list-of-hosts) trimmed-hosts) :list-of-hosts)))

(defmulti split-hosts split-hosts-dispatch)

(defmethod split-hosts :list-of-hosts [hosts-string]
  (let [trimmed-hosts (trim-hosts hosts-string)
        hosts (->> trimmed-hosts
                   (re-find (re-pattern (case-to-regex :list-of-hosts)))
                   peek)
        hosts-with-normalized-dot (.replace hosts "[.]" ".")]
    (split hosts-with-normalized-dot #"[|]")))

(defn- combine-prefix-mid-suffix [prefixes middle suffixes]
  (let [hosts (for [prefix (split prefixes #"[|]")
                    suffix (split suffixes #"[|]")]
                (str prefix middle suffix))
        hosts-with-normalized-dot (map #(.replace % "[.]" ".") hosts)]
    hosts-with-normalized-dot))

(defmethod split-hosts :prefix-and-suffix [hosts-string]
  (let [trimmed-hosts (trim-hosts hosts-string)
        [_ prefixes middle suffixes]
        (->> trimmed-hosts
             (re-find (re-pattern (case-to-regex :prefix-and-suffix))))]
    (combine-prefix-mid-suffix prefixes middle suffixes)))

(defmethod split-hosts :prefix [hosts-string]
  (let [trimmed-hosts (trim-hosts hosts-string)
        [_ prefixes middle]
        (->> trimmed-hosts
             (re-find (re-pattern (case-to-regex :prefix))))]
    (combine-prefix-mid-suffix prefixes middle "")))

(defmethod split-hosts :suffix [hosts-string]
  (let [trimmed-hosts (trim-hosts hosts-string)
        [_ middle suffixes]
        (->> trimmed-hosts
             (re-find (re-pattern (case-to-regex :suffix))))]
    (combine-prefix-mid-suffix "" middle suffixes)))

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
