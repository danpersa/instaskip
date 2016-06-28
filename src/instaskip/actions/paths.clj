(in-ns 'instaskip.actions)

(defn list-paths [filters innkeeper-config]

  (let [paths (match filters
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

    (table paths-with-hosts)))


(defn list-hosts-for-path [path-id innkeeper-config]
  (let [{:keys [uri host-ids]} (ik/get-path path-id innkeeper-config)
        ids-to-hosts (ik/ids-to-hosts innkeeper-config)
        hosts (map (fn [id] {:hosts (ids-to-hosts id)}) host-ids)]
    (println "Hosts for path with uri: " uri)
    (table hosts)))
