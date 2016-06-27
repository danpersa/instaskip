(ns instaskip.core
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.core.match :refer [match]]
            [clojure.tools.logging :as log]
            [clojure.string :as string]
            [instaskip.migrate :as migrate]
            [defun :refer [defun defun-]]
            [instaskip.actions.create :as create])
  (:gen-class :main true))

(def ^{:private true} cli-options
  [["-u" "--url URL" "The url for innkeeper" :default "http://localhost:9080"]
   ["-t" "--token TOKEN" "The OAuth token"]
   ["-T" "--team TEAM" "The name of the team. Optional for actions: migrate-routes create list-paths list-routes"]
   ["-d" "--dir DIR" "The directory with the eskip files. For action: migrate-routes"]
   ["-R" "--route ROUTE" "An eskip route. For action: create"]
   ["-h" "--help" "Displays this" :default false]])

(defn- exit [status msg]
  (println msg)
  ;(System/exit status)
  )

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
        "  list-routes     Lists the routes. Can be filtered by team"]
       (string/join \newline)))

(defn- migrate-routes [opts url token]
  (match opts
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
  (match opts
         {:route route :team team}
         ; =>
         (create/create route team {:innkeeper-url url
                                    :oauth-token   token})

         :else
         ; =>
         (exit 1 "Invalid options for create")))

(defn- list-paths [opts url token]
  (println "List paths. Under construction."))

(defn- list-routes [opts url token]
  (println "List routes. Under construction."))

(defn- parse-action [params url token]
  (let [options (params :options)]
    (match params
           {:arguments ["migrate-routes"]} (migrate-routes options url token)
           {:arguments ["create"]} (create options url token)
           {:arguments ["list-paths"]} (list-paths options url token)
           {:arguments ["list-routes"]} (list-routes options url token)
           :else (exit 1 "Invalid action"))))

(defn -main
  "The application's main function"
  [& args]
  (let [params (parse-opts args cli-options :in-order false)]
    (log/debug params)
    (match params
           {:options {:help true}} (exit 0 (usage (params :summary)))
           {:options {:url url :token token}} (parse-action params url token)
           :else (exit 1 "Invalid options"))))
