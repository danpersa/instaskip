(in-ns 'instaskip.actions)

(defn list-routes [filters innkeeper-config]

  (let [routes-try (exc/try-on
                     (m/match filters
                              {:team team}
                              (ik/get-routes {:owned-by-team team} innkeeper-config)

                              :else
                              (ik/get-routes innkeeper-config)))]

    (m/matchm routes-try
              {:success routes}
              (let [filtered-routes (map #(select-keys % [:id :name :endpoint]) routes)]
                (cl/info "List routes")
                (t/table filtered-routes))

              {:failure ex}
              (cl/error ex))))

(defn list-route [id innkeeper-config]
  (let [route-try (exc/try-on (ik/get-route id innkeeper-config))]

    (m/matchm route-try
              {:success route}
              (let [filtered-route (select-keys route [:id :name :path-id :created-by
                                                       :activate-at :created-at
                                                       :uses-common-filters :endpoint])]
                (cl/info "List route wiht id" id)
                (t/table filtered-route)
                (cl/info "Filters for the route")
                (t/table (route :filters))
                (cl/info "Predicates for the route")
                (t/table (route :predicates)))

              {:failure ex}
              (cl/error ex))))


(defn delete-route [id innkeeper-config]
  (cl/info "Deleting route with id" id)
  (let [delete-try
        (exc/try-on (ik/delete-route id innkeeper-config))]

    (m/matchm delete-try
              {:success delete}
              (cl/info "Done")

              {:failure ex}
              (cl/error ex))))
