(in-ns 'instaskip.actions)

(defn create-innkeeper-route-with-path
  [{:keys [route path]} innkeeper-config]

  (let [path-uri (path :uri)
        existing-path ((ik/path-uris-to-paths innkeeper-config) path-uri)]
    (if (not (nil? existing-path))
      (let [path-id (existing-path :id)
            innkeeper-route (assoc route :path-id path-id)
            route-name (innkeeper-route :name)
            new-host-ids (path :host-ids)
            existing-host-ids (existing-path :host-ids)]
        (cl/info "Found existing path with uri:" (existing-path :uri))

        (if (not= (set existing-host-ids) (set new-host-ids))
          (do (cl/info "Updating host-ids from " (sort existing-host-ids) "to" (sort new-host-ids))
              (ik/patch-path path-id {:host-ids new-host-ids} innkeeper-config)))
        (let [existing-routes (ik/get-routes {:name route-name} innkeeper-config)]
          (if (empty? existing-routes)
            (do (cl/info "Posting a new route with name: " route-name)
                (ik/post-route innkeeper-route innkeeper-config))
            (cl/info "Found existing route with name:" route-name))))

      (let [innkeeper-path (ik/post-path path innkeeper-config)
            innkeeper-route (assoc route :path-id (innkeeper-path :id))]
        (cl/info "Posting a new path with uri: " (innkeeper-path :uri))
        (cl/info "Posting a new route with name: " (innkeeper-route :name))
        (ik/post-route innkeeper-route innkeeper-config)))))

(defn create
  [eskip team innkeeper-config]
  (let [eskip-map (eskip/eskip->map eskip)
        route-with-path (em/eskip-map->route-with-path team eskip-map)
        innkeeper-route-with-path-try (r/route-with-path->innkeeper-route-with-path innkeeper-config route-with-path)]

    (m/matchm innkeeper-route-with-path-try
              {:success innkeeper-route-with-path}
              (create-innkeeper-route-with-path innkeeper-route-with-path innkeeper-config)

              {:failure ex}
              (cl/error ex))))
