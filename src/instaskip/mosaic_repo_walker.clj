(ns instaskip.mosaic-repo-walker
  (:require [me.raynes.fs :as fs]
            [cats.core :as m]
            [cats.builtin]
            [instaskip.impl.from-eskip :as eskip]
            [instaskip.impl.to-innkeeper :as ik]))


(def routes-dir "/Users/dpersa/Prog/mosaic/mosaic-staging/routes/")
(def team-names
  "Reads the routes dir and returns a list with the names of the teams"
  (map #(.getName %)
       (fs/list-dir routes-dir)))

(defn to-map [team-name route-files]
  {:team-name   team-name
   :route-files route-files})

(comment
  (to-map "team-name" '("file1" "file2")))

(def team-names-with-files
  (m/alet [team-name team-names]
          (->> (str routes-dir team-name)
               fs/list-dir
               (map (fn [arg] {:team-name team-name :file-name (.getName arg)})))))


(def team-with-eskip-file
  (vec (flatten team-names-with-files)))

(def team-with-eskip
  (m/alet [{team-name :team-name file-name :file-name} team-with-eskip-file]

          {:team-name team-name :eskip (slurp (str routes-dir team-name "/" file-name))}))

(def team-with-eskip-map
  (vec (flatten (m/alet [{team-name :team-name eskip :eskip} team-with-eskip]
                        {:team-name team-name :eskip-maps (eskip/eskip->maps eskip)}))))

(comment team-with-eskip-map)


(comment (def eskip-maps (:eskip-maps (first team-with-eskip-map))))


(defn eskip-maps-without-regex-paths [eskip-maps]
  (filter
    (fn [arg]
      (not (empty?
             (filter #(and (= (:name %) "Path")
                           (not (.contains
                                  (:value (first (:args %)))
                                  "*"
                                  ))) (:predicates arg)))))
    eskip-maps))

(comment (eskip-maps-without-regex-paths eskip-maps))


(for [{team-name :team-name eskip-maps :eskip-maps} team-with-eskip-map
      eskip-map (eskip-maps-without-regex-paths eskip-maps)]
  (ik/eskip-map-to-innkeeper eskip-map))