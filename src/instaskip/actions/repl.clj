(in-ns 'instaskip.actions)

(def ^:private cli-options
  [["-u" "--url URL" "The url for innkeeper" :default "http://localhost:9080"]
   ["-t" "--token TOKEN" "The OAuth token"]
   ["-T" "--team TEAM" "The name of the team. Optional for actions: migrate-routes create list-paths list-routes"]
   ["-d" "--dir DIR" "The directory with the routes in eskip format. Eg: ~/mosaic-staging/routes For action: migrate-routes"]
   ["-R" "--route ROUTE" "An eskip route. For action: create"]
   ["-i" "--id ID" "An id. For actions: hosts-for-path"]])

(defn- usage [options-summary]
  (->> ["This is the innkeeper REPL."
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  help                  Displays this."
        "  list-hosts            Lists the hosts."]
       (string/join \newline)))

(defn my-println [& more]
  (.write *out* (str (clojure.string/join " " more) "\n"))
  (flush))

(defn my-print [& more]
  (.write *out* (str (clojure.string/join " " more)))
  (flush))

(defn repl-stdin [chan]
  (loop [line (read-line)]
    (async/>!! chan line)
    (if (not= "exit" line)
      (recur (read-line)))))

(defn repl [chan innkeeper-config]
  (my-println "Starting the repl")
  (my-print "# ")
  (async/go-loop [line (async/<! chan)]

    (if (not= "exit" line)
      (let [args (string/split line #"[ ]")
            params (parse-opts args cli-options :in-order false)]

        ;(my-println params)

        (m/match params
                 {:arguments ["help"]}
                 (my-println (usage (params :summary)))
                 {:arguments ["list-hosts"]}
                 (list-hosts innkeeper-config)
                 :else (my-println "Invalid options"))

        (my-print "# ")
        (recur (async/<! chan))))))
