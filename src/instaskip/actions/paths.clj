(in-ns 'instaskip.actions)

(defn list-paths [filters innkeeper-config]

  (let [paths-try (exc/try-on
                    (m/match filters
                             {:team team}
                             (ik/get-paths
                               {:owned-by-team team} innkeeper-config)

                             :else
                             (ik/get-paths innkeeper-config)))]

    (m/matchm paths-try
              {:success paths}
              (let [paths-with-hosts (->> paths
                                          (map (fn [{:keys [id uri]}]
                                                 {:id  id
                                                  :uri uri}))
                                          vec)]
                (t/table paths-with-hosts))

              {:failure ex}
              (cl/error ex))))

(defn list-path [path-id innkeeper-config]
  (let [path-try (exc/try-on
                   (ik/get-path path-id innkeeper-config))]

    (m/matchm path-try
              {:success path}
              (let [{:keys [id uri host-ids]} path
                    ids-to-hosts (ik/ids-to-hosts innkeeper-config)
                    hosts (map (fn [id] {:hosts (ids-to-hosts id)}) host-ids)]
                (cl/info "Path with id" id)
                (t/table (select-keys path [:id :uri :owned-by-team
                                            :created-by :created-at :updated-at]))
                (cl/info "Hosts for path with uri: " uri)
                (t/table hosts))

              {:failure ex}
              (cl/error ex))))
