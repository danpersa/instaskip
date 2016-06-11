(ns instaskip.innkeeper-config)

(def innkeeper-url "http://localhost:8080")
(def hosts-url (str innkeeper-url "/hosts"))

(def read-token (str "Bearer " "token-user~1-employees-route.read"))
(def write-token (str "Bearer " "token-user~1-employees-route.write"))
(def admin-token (str "Bearer " "token-user~1-employees-route.admin"))
