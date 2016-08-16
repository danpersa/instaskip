(ns instaskip.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.core.match :as m]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [instaskip.actions.migrate :as migrate]
            [instaskip.actions :as actions]
            [instaskip.console-log :as cl]
            [clojure.core.async :as async])

  (:gen-class :main true))

(def ^{:private true} cli-options
  [["-u" "--url URL" "The url for innkeeper" :default "http://localhost:9080"]
   ["-t" "--token TOKEN" "The OAuth token"]
   ["-T" "--team TEAM" "The name of the team. Optional for actions: migrate-routes create list-paths list-routes"]
   ["-d" "--dir DIR" "The directory with the routes in eskip format. Eg: ~/mosaic-staging/routes For action: migrate-routes"]
   ["-R" "--route ROUTE" "An eskip route. For action: create"]
   ["-i" "--id ID" "An id. For actions: hosts-for-path"]
   ["-h" "--help" "Displays this" :default false]])

(defn- exit
  ([status msg]
   (cl/info msg)
   (System/exit status))
  ([status] (System/exit status)))

(defn- usage [options-summary]
  (->> ["This is the innkeeper cli."
        ""
        "Usage: instaskip [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  migrate-routes        Migrates the routes from an eskip directory to innkeeper."
        "  create                Posts an eskip path and route to innkeeper."
        "  list-paths            Lists the paths. Can be filtered by team."
        "  list-path             Details for a path. Needs a path id."
        "  list-routes           Lists the routes. Can be filtered by team."
        "  list-route            Lists a specific route. Needs an id."
        "  delete-route          Deletes a specific route. Needs an id."
        "  repl                  Starts the instakip REPL."
        "  list-hosts            Lists the hosts."]
       (string/join \newline)))

(defn- migrate-routes [opts innkeeper-config]
  (m/match opts
           {:dir dir :team team}
           (migrate/routes dir [team] innkeeper-config)

           {:dir dir}
           (migrate/routes dir innkeeper-config)

           :else
           (exit 1 "Invalid options for migrate-routes. The -d flag is mandatory.")))

(defn- create [opts innkeeper-config]
  (m/match opts
           {:route route :team team}
           (actions/create route team innkeeper-config)

           :else
           (exit 1 "Invalid options for create. Both -R and -T flags should be present.")))

(defn- list-paths [opts innkeeper-config]
  (m/match opts
           {:team team}
           (do (cl/info "List paths for team:" team)
               (actions/list-paths {:team team} innkeeper-config))
           {}
           (do (cl/info "List all paths")
               (actions/list-paths {} innkeeper-config))
           :else
           (exit 1 "Invalid options for create")))

(defn- list-path [opts innkeeper-config]
  (m/match opts
           {:id path-id}
           (actions/list-path (Integer. path-id) innkeeper-config)

           :else
           (exit 1 "Invalid options for hosts-for-path. The -i flag should be present.")))

(defn- list-hosts [innkeeper-config]
  (actions/list-hosts innkeeper-config))

(defn- list-routes [opts innkeeper-config]
  (m/match opts
           {:team team}
           (actions/list-routes {:team team} innkeeper-config)

           {}
           (actions/list-routes {} innkeeper-config)

           :else
           (exit 1 "Invalid options for list-routes.")))

(defn- list-route [opts innkeeper-config]
  (m/match opts
           {:id id}
           (actions/list-route (Integer. id) innkeeper-config)

           :else
           (exit 1 "Invalid options for list-route. The -i flag should be present.")))

(defn- delete-route [opts innkeeper-config]
  (m/match opts
           {:id id}
           (actions/delete-route (Integer. id) innkeeper-config)

           :else
           (exit 1 "Invalid options for list-route. The -i flag should be present.")))

(defn- repl [opts innkeeper-config]
  (m/match opts

           {}
           (let [chan (async/chan 1)]
             (actions/repl chan innkeeper-config)
             (actions/repl-stdin chan))

           :else
           (exit 1 "Invalid options for repl.")))

(defn- parse-action [params url token]
  (let [options (params :options)
        innkeeper-config {:innkeeper-url url
                          :oauth-token   token}]
    (m/match params
             {:arguments ["migrate-routes"]} (migrate-routes options innkeeper-config)
             {:arguments ["create"]} (create options innkeeper-config)
             {:arguments ["list-paths"]} (list-paths options innkeeper-config)
             {:arguments ["list-path"]} (list-path options innkeeper-config)
             {:arguments ["list-routes"]} (list-routes options innkeeper-config)
             {:arguments ["list-route"]} (list-route options innkeeper-config)
             {:arguments ["delete-route"]} (delete-route options innkeeper-config)
             {:arguments ["list-hosts"]} (list-hosts innkeeper-config)
             {:arguments ["repl"]} (repl {} innkeeper-config)
             :else (exit 1 "Invalid action"))))

(defn -main
  "The application's main function"
  [& args]
  (println args)
  (let [params (parse-opts args cli-options :in-order false)]
    (log/debug params)
    (m/match params
             {:options {:help true}} (exit 0 (usage (params :summary)))
             {:options {:url url :token token}} (parse-action params url token)
             :else (exit 1 "Invalid options")))
  (exit 0))
