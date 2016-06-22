(ns instaskip.route-with-path-process
  [:require [instaskip.innkeeper-client :as ik]])


(defn- transform-hosts-to-ids [hosts innkeeper-config]
  (let [hosts-to-ids (ik/hosts-to-ids innkeeper-config)]
    (vec (map (fn [host] (hosts-to-ids host)) hosts))))

(defn- path-with-hosts->path-with-host-ids [path innkeeper-config]
  {:uri           (path :uri)
   :host-ids      (transform-hosts-to-ids (path :hosts) innkeeper-config)
   :owned-by-team (path :owned-by-team)})

(defn route-with-path->innkeeper-route-with-path
  "Transforms a route-with-path to the innkeeper format.
   It uses the innkeeper-client to call innkeeper.
   If the path exists, add the :path-id to the route.
   If not transforms the :hosts for the path to :host-ids"

  [innkeeper-config {:keys [route path]}]

  {:route route
   :path  (path-with-hosts->path-with-host-ids path innkeeper-config)})
