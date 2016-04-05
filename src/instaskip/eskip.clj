(ns instaskip.eskip
  (:require [instaskip.impl.from-eskip :as from-eskip :only [eskip->json single-eskip->json]]
            [instaskip.impl.from-json :as from-json :only [json->eskip]]
            [clojure.data.json :as json])
  (:gen-class
    :name instaskip.Eskip
    :methods [#^{:static true} [eskipToJson [String] String]
              #^{:static true} [singleEskipToJson [String] String]
              #^{:static true} [jsonToEskip [String] String]]
    :main false))

(defn eskip->json
  "Transforms an eskip routes string to a json array string"
  [eskip-routes]

  (from-eskip/eskip->json eskip-routes))

(defn -eskipToJson
  "Transforms an eskip routes string to a json string - java wrapper"
  [eskip-routes]

  (eskip->json eskip-routes))

(defn single-eskip->json
  "Transforms an eskip route string to a json string"
  [eskip-routes]

  (from-eskip/single-eskip->json eskip-routes))

(defn -singleEskipToJson
  "Transforms an eskip route string to a json string - java wrapper"
  [eskip-routes]

  (single-eskip->json eskip-routes))

(defn json->eskip
  "Transforms a json string into an eskip routes string"
  [eskip-json]

  (from-json/json->eskip eskip-json))

(defn -jsonToEskip
  "Transforms a json string into an eskip routes string - java wrapper"
  [eskip-routes]

  (json->eskip eskip-routes))
