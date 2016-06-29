(ns instaskip.actions
  (:require [instaskip.impl.from-eskip :as eskip]
            [instaskip.impl.eskip-map-process :as em]
            [instaskip.route-with-path-process :as r]
            [instaskip.innkeeper-client :as ik]
            [clojure.core.match :as m]
            [table.core :as t]
            [clojure.string :as string]))

(load "actions/create")
(load "actions/paths")
(load "actions/routes")
(load "actions/hosts")
