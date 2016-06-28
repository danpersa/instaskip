(ns instaskip.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.core.match :as m]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [instaskip.migrate :as migrate]
            [instaskip.actions :as actions])

  (:gen-class :main true))

(def ^{:private true} cli-options
  [["-u" "--url URL" "The url for innkeeper" :default "http://localhost:9080"]
   ["-t" "--token TOKEN" "The OAuth token"]
   ["-T" "--team TEAM" "The name of the team. Optional for actions: migrate-routes create list-paths list-routes"]
   ["-d" "--dir DIR" "The directory with the eskip files. For action: migrate-routes"]
   ["-R" "--route ROUTE" "An eskip route. For action: create"]
   ["-i" "--id ID" "An id. For actions: hosts-for-path"]
   ["-h" "--help" "Displays this" :default false]])

(defn- exit
  ([status msg]
   (println msg)
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
        "  migrate-routes  Migrates the routes from an eskip directory to innkeeper"
        "  create          Posts an eskip path and route to innkeeper"
        "  list-paths      Lists the paths. Can be filtered by team"
        "  hosts-for-path  Hosts for path. Needs an path id"
        "  list-routes     Lists the routes. Can be filtered by team"]
       (string/join \newline)))

(defn- migrate-routes [opts url token]
  (m/match opts
           {:dir dir :team team}
           ; =>
           (migrate/routes dir [team] {:innkeeper-url url
                                       :oauth-token   token})

           {:dir dir}
           ; =>
           (migrate/routes dir {:innkeeper-url url
                                :oauth-token   token})

           :else
           ; =>
           (exit 1 "Invalid options for migrate-routes")))

(defn- create [opts url token]
  (m/match opts
           {:route route :team team}
           ; =>
           (actions/create route team {:innkeeper-url url
                                       :oauth-token   token})

           :else
           ; =>
           (exit 1 "Invalid options for create")))

(defn- list-paths [opts url token]
  (m/match opts
           {:team team}
           ; =>
           (do (println "List paths for team:" team)
               (actions/list-paths {:team team} {:innkeeper-url url
                                                 :oauth-token   token}))
           {}
           ; =>
           (do (println "List all paths")
               (actions/list-paths {} {:innkeeper-url url
                                       :oauth-token   token}))
           :else
           ; =>
           (exit 1 "Invalid options for create")))

(defn- hosts-for-path [opts url token]
  (m/match opts
           {:id path-id}
           ; =>
           (actions/list-hosts-for-path (Integer. path-id) {:innkeeper-url url
                                                            :oauth-token   token})

           :else
           ; =>
           (exit 1 "Invalid options for hosts-for-path")))

(defn- list-routes [opts url token]
  (m/match opts
           {:team team}
           ; =>
           (actions/list-routes team {:innkeeper-url url
                                      :oauth-token   token})

           :else
           ; =>
           (exit 1 "Invalid options for create")))

(defn- parse-action [params url token]
  (let [options (params :options)]
    (m/match params
             {:arguments ["migrate-routes"]} (migrate-routes options url token)
             {:arguments ["create"]} (create options url token)
             {:arguments ["list-paths"]} (list-paths options url token)
             {:arguments ["hosts-for-path"]} (hosts-for-path options url token)
             {:arguments ["list-routes"]} (list-routes options url token)
             :else (exit 1 "Invalid action"))))

(defn -main
  "The application's main function"
  [& args]
  (let [params (parse-opts args cli-options :in-order false)]
    (log/debug params)
    (m/match params
             {:options {:help true}} (exit 0 (usage (params :summary)))
             {:options {:url url :token token}} (parse-action params url token)
             :else (exit 1 "Invalid options")))
  (exit 0))
