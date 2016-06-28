(ns instaskip.migrate
  (:require [me.raynes.fs :as fs]
            [cats.builtin]
            [instaskip.impl.from-eskip :as eskip]
            [instaskip.impl.eskip-map-process :as em]
            [instaskip.route-with-path-process :as r]
            [clojure.spec :as s]
            [clojure.string :as string]
            [instaskip.actions :as actions]))

(defn- team-names-in-dir
  "Reads the routes dir and returns a list with the names of the teams"

  [routes-dir]
  (map #(.getName %)
       (fs/list-dir routes-dir)))

(s/def :m/team string?)
(s/def :m/eskip string?)
(s/def :m/team-to-eskip (s/keys :req-un [:m/team :m/eskip]))
(s/def :m/teams-with-eskip (s/* :m/team-to-eskip))

(s/fdef teams-with-eskip :ret :m/teams-with-eskip)

(defn- teams-with-eskip
  "Looks in the routes-dir for the specified teams.
   Returns a vec of maps with teams and eskip markup.
   Skips the empty files"

  [team-names routes-dir]
  (->> team-names
       (map (fn [team]
              {:team     team
               :team-dir (str routes-dir "/" team)}))
       (mapcat (fn [{:keys [team team-dir]}]
                 (->> (fs/list-dir team-dir)
                      (map (fn [arg] {:team       team
                                      :eskip-file (str routes-dir "/" team "/" (.getName arg))})))))
       (map (fn [{:keys [team eskip-file]}]
              {:team team :eskip (-> eskip-file
                                     slurp
                                     string/trim)}))
       (filter (fn [{:keys [_ eskip]}] (not (empty? eskip))))
       vec))

(s/instrument #'teams-with-eskip)

(defn- teams-with-eskip-maps
  "Transforms the teams with eskip vec into a teams with eskip-map vec"

  [teams-with-eskip]
  (->> teams-with-eskip
       (mapcat (fn [{:keys [team eskip]}]
                 (->> (eskip/eskip->maps eskip)
                      (map (fn [eskip-map] {:team      team
                                            :eskip-map eskip-map})))))
       vec))

(defn- path-without-star-predicate?
  "Returns true if the eskip predicate is a Path and the value
   doesn't contain a *"

  [eskip-predicate]
  (and (= (:name eskip-predicate) "Path")
       (let [first-arg-value (:value (first (:args eskip-predicate)))]
         (-> first-arg-value
             (.contains "*")
             not))))

(defn- host-predicate?
  "Returns true if the eskip predicate is a Host"

  [eskip-predicate]
  (= (:name eskip-predicate) "Host"))

(defn- has-element? [col predicate]
  (->> col
       (filter predicate)
       first
       nil?
       not))

(defn- filter-teams-with-eskip-maps
  "Returns only the teams with eskip maps with an eskip Path predicate present
   and without a * in the eskip Path predicate.
   Removes the maps without an eskip Host predicate."

  [teams-with-eskip-maps]
  (filter
    (fn [{:keys [eskip-map]}]
      (let [predicates (:predicates eskip-map)]
        (and (has-element? predicates host-predicate?)
             (has-element? predicates path-without-star-predicate?))))
    teams-with-eskip-maps))

(defn- routes-with-paths
  "Transforms a vec of teams-with-eskip-maps into a vec of routes-with-paths"

  [teams-with-eskip-maps]
  (->> teams-with-eskip-maps
       (map (fn [{:keys [team eskip-map]}]
              (em/eskip-map->route-with-path team eskip-map)))))


(defn- innkeeper-routes-with-paths
  "Transforms a vec of routes-with-paths into a vec of innkeeper-routes-with-paths"

  [routes-with-paths innkeeper-config]

  (->> routes-with-paths
       (map (partial r/route-with-path->innkeeper-route-with-path innkeeper-config))))

(defn routes
  "Migrates the routes in the dir to the innkeeper instance with the specified url, using the token.
   The token should have innkeeper admin scope"

  ([routes-dir teams innkeeper-config]
   (println "Migrate routes. Eskip dir: " routes-dir
            "\nTeams: " teams
            "\nInnkeeper url: " (innkeeper-config :url)
            "\nOAuth token: " (innkeeper-config :token))

   (let [teams-with-eskip (teams-with-eskip teams routes-dir)
         teams-with-eskip-maps (teams-with-eskip-maps teams-with-eskip)
         filtered-teams-with-eskip-maps (filter-teams-with-eskip-maps teams-with-eskip-maps)
         routes-with-paths (routes-with-paths filtered-teams-with-eskip-maps)
         innkeeper-routes-with-paths (innkeeper-routes-with-paths routes-with-paths innkeeper-config)]

     (println "Found" (count teams) "team(s).")
     (println "Found" (count teams-with-eskip) "eskip file(s).")
     (println "Transformed" (count teams-with-eskip-maps) "to eskip maps.")
     (println "Filtered to" (count filtered-teams-with-eskip-maps) "eskip maps.")
     (println "Transformed eskip maps to" (count routes-with-paths) "routes with paths.")
     (println "Transformed routes with paths to"
              (count innkeeper-routes-with-paths)
              "innkeeper routes with paths.")

     (doseq [route-with-path innkeeper-routes-with-paths]
       (actions/create-innkeeper-route-with-path route-with-path innkeeper-config))))

  ([routes-dir innkeeper-config]
   (routes routes-dir innkeeper-config (team-names-in-dir routes-dir))))

