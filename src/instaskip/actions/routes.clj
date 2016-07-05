(in-ns 'instaskip.actions)

(defn list-routes [filters innkeeper-config]

  (let [routes (m/match filters
                        {:team team}
                        (ik/get-routes {:owned-by-team team} innkeeper-config)

                        :else
                        (ik/get-routes innkeeper-config))
        filtered-routes (map #(select-keys % [:id :name :endpoint]) routes)]
    (cl/info "List routes")
    (t/table filtered-routes)))

(defn list-route [id innkeeper-config]
  (let [route (ik/get-route id innkeeper-config)
        filtered-route (select-keys route [:id :name :path-id :created-by
                                           :activate-at :created-at
                                           :uses-common-filters :endpoint])]
    (cl/info "List route wiht id" id)
    (t/table filtered-route)
    (cl/info "Filters for the route")
    (t/table (route :filters))
    (cl/info "Predicates for the route")
    (t/table (route :predicates))))


(defn delete-route [id innkeeper-config]
  (cl/info "Deleting route with id" id)
  (ik/delete-route id innkeeper-config)
  (cl/info "Done"))
