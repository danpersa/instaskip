(ns instaskip.route-with-path-process
  [:require [instaskip.innkeeper-client :as ik]
            [cats.core :as c]
            [cats.builtin]
            [cats.monad.exception :as exc]
            [cats.context :as ctx]])

(defn- host->host-id [hosts-to-ids host]

  (exc/try-on
    (let [id (hosts-to-ids host)]
      (if (nil? id)
        (throw (ex-info (str "Host " host " not defined in innkeeper!") {:host host})))
      id)))

(defn- hosts->ids [hosts innkeeper-config]
  (let [hosts-to-ids (ik/hosts-to-ids innkeeper-config)
        fun (partial host->host-id hosts-to-ids)]
    (ctx/with-context exc/context
                      (c/traverse fun hosts))))

(defn- path-with-hosts->path-with-host-ids [path innkeeper-config]

  (let [host-ids-try (hosts->ids (path :hosts) innkeeper-config)]
    (->> host-ids-try
         (c/fmap (fn [host-ids] {:uri           (path :uri)
                                 :host-ids      host-ids
                                 :owned-by-team (path :owned-by-team)})))))

(defn route-with-path->innkeeper-route-with-path
  "Transforms a route-with-path to the innkeeper format.
   It uses the innkeeper-client to call innkeeper.
   If not transforms the :hosts for the path to :host-ids.
   Returns Success in case everything was fine, Failure otherwise"

  [innkeeper-config {:keys [route path]}]

  (let [path-with-host-ids-try (path-with-hosts->path-with-host-ids path innkeeper-config)]

    (->> path-with-host-ids-try
         (c/fmap (fn [path-with-host-ids]
                   {:route route
                    :path  path-with-host-ids})))))
