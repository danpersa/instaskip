(ns instaskip.actions
  (:require [instaskip.impl.from-eskip :as eskip]
            [instaskip.impl.eskip-map-process :as em]
            [instaskip.route-with-path-process :as r]
            [instaskip.innkeeper-client :as ik]
            [clojure.core.match :as m]
            [table.core :as t]
            [clojure.string :as string]
            [cats.monad.exception :as exc]
            [instaskip.console-log :as cl]))

(load "actions/create")
(load "actions/paths")
(load "actions/routes")
(load "actions/hosts")
