(in-ns 'instaskip.actions)

(defn list-paths [filters innkeeper-config]

  (let [paths (m/match filters
                       {:team team}
                       ; =>
                       (ik/get-paths {:owned-by-team team} innkeeper-config)

                       :else
                       ; =>
                       (ik/get-paths innkeeper-config))

        paths-with-hosts (->> paths
                              (map (fn [{:keys [id uri]}]
                                     {:id  id
                                      :uri uri}))
                              vec)]

    (t/table paths-with-hosts)))

(defn list-path [path-id innkeeper-config]
  (let [path (ik/get-path path-id innkeeper-config)
        {:keys [id uri host-ids]} path
        ids-to-hosts (ik/ids-to-hosts innkeeper-config)
        hosts (map (fn [id] {:hosts (ids-to-hosts id)}) host-ids)]
    (println "Path with id" id)
    (t/table (select-keys path [:id :uri :owned-by-team :created-by :created-at :updated-at]))
    (println "Hosts for path with uri: " uri)
    (t/table hosts)))
