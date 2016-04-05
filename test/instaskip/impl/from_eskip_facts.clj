(ns instaskip.impl.from-eskip-facts
  (:require [midje.sweet :refer :all]
            [instaskip.impl.from-eskip :refer :all]
            [midje.util :refer [expose-testables]]
            [clojure.string :refer [join]]))

(expose-testables instaskip.impl.from-eskip)

(fact "str->num should transform a string representing an Integer to a number"
  (str->num "4") => 4)

(fact "str->num should transform a string representing an Double to a number"
  (str->num "4.5") => 4.5)

(fact "eskip->json parses a match all route"
  (eskip->json "hello1: * -> <shunt>;") =>
    (str "[{\"name\":\"hello1\","
         "\"predicates\":[{\"name\":\"*\",\"args\":[]}],"
         "\"filters\":[],"
         "\"endpoint\":\"\"}]"))

(fact "eskip->json parses a simple route"
  (eskip->json "hello1: pred1() -> <shunt>;") =>
    (str "[{\"name\":\"hello1\","
         "\"predicates\":[{\"name\":\"pred1\",\"args\":[]}],"
         "\"filters\":[],"
         "\"endpoint\":\"\"}]"))

(fact "eskip->json parses a simple route wiht an endpoint"
      (eskip->json "hello1: pred1() -> \"http://www.hello.com/\";") =>
      (str "[{\"name\":\"hello1\","
           "\"predicates\":[{\"name\":\"pred1\",\"args\":[]}],"
           "\"filters\":[],"
           "\"endpoint\":\"http:\\/\\/www.hello.com\\/\"}]"))

(fact "eskip->json parses a route with a predicate"
  (eskip->json "hello1: pred1(\"hello\", 3, 4.2, 0, /^.*$/) -> <shunt>;") =>
    (str "[{\"name\":\"hello1\","
         "\"predicates\":[{\"name\":\"pred1\",\"args\":[\"hello\",3,4.2,0,\"\\/^.*$\\/\"]}],"
         "\"filters\":[],"
         "\"endpoint\":\"\"}]"))

(fact "eskip->json parses a route with many predicates"
  (eskip->json "hello1: pred1(\"hello\") && pred2(2) -> <shunt>;") =>
      (str "[{\"name\":\"hello1\","
           "\"predicates\":[{\"name\":\"pred1\",\"args\":[\"hello\"]},{\"name\":\"pred2\",\"args\":[2]}],"
           "\"filters\":[],"
           "\"endpoint\":\"\"}]"))

(fact "eskip->json parses a route with a filter"
  (eskip->json "hello1: pred1() -> filter1(\"hello\", 3, 4.2, 0) -> <shunt>;") =>
    (str "[{\"name\":\"hello1\","
         "\"predicates\":[{\"name\":\"pred1\",\"args\":[]}],"
         "\"filters\":[{\"name\":\"filter1\",\"args\":[\"hello\",3,4.2,0]}],"
         "\"endpoint\":\"\"}]"))

(fact "eskip->json parses a route with many filters"
  (eskip->json "hello1: pred1() -> filter1(\"hello\") -> filter2(2) -> <shunt>;") =>
    (str "[{\"name\":\"hello1\"," 
         "\"predicates\":[{\"name\":\"pred1\",\"args\":[]}],"
         "\"filters\":[{\"name\":\"filter1\",\"args\":[\"hello\"]},{\"name\":\"filter2\",\"args\":[2]}],"
         "\"endpoint\":\"\"}]"))

(fact "eskip->json should parse more routes"
  (eskip->json (str "hello1: pred1(\"hello\") -> <shunt>;"
                    "hello2: * -> \"http://hello.com\";")) =>
      (str "[{\"name\":\"hello1\","
           "\"predicates\":[{\"name\":\"pred1\",\"args\":[\"hello\"]}],"
           "\"filters\":[],\"endpoint\":\"\"},"
           "{\"name\":\"hello2\","
           "\"predicates\":[{\"name\":\"*\",\"args\":[]}],"
           "\"filters\":[],"
           "\"endpoint\":\"http:\\/\\/hello.com\"}]"))

(fact "single-eskip->json parses a simple route"
      (single-eskip->json "hello1: pred1() -> <shunt>;") =>
      (str "{\"name\":\"hello1\","
           "\"predicates\":[{\"name\":\"pred1\",\"args\":[]}],"
           "\"filters\":[],"
           "\"endpoint\":\"\"}"))