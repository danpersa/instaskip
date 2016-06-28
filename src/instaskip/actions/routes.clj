(in-ns 'instaskip.actions)

(defn list-routes [filters innkeeper-config]

  (match filters
         {:team team}
         ; =>
         (ik/get-paths {:owned-by-team team} innkeeper-config)

         :else
         ; =>
         (ik/get-paths innkeeper-config)))
