(ns instaskip.impl.from-json-facts
  (:require [instaskip.impl.from-json :refer :all]
            [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]]))

(expose-testables instaskip.impl.from-json)

(def eskip-json
  (str "{\"name\":\"hello1\","
       "\"predicates\":[{\"name\":\"hello\",\"args\":[1,\"hello\",\"\\/^.*$\\/\"]},{\"name\":\"hello1\",\"args\":[]}],"
       "\"filters\":[{\"name\":\"filter1\",\"args\":[1,\"hello\"]},{\"name\":\"filter2\",\"args\":[]}],"
       "\"endpoint\":\"http://www.hello.com/hello\"}"))

(def eskip-json-small
  (str "{\"name\":\"hello1\","
       "\"predicates\":[],"
       "\"filters\":[],"
       "\"endpoint\":\"\"}"))

(def eskip-json-array
  (str "[{\"name\":\"hello1\","
       "\"predicates\":[],"
       "\"filters\":[],"
       "\"endpoint\":\"\"},"
       "{\"name\":\"hello2\","
       "\"predicates\":[],"
       "\"filters\":[],"
       "\"endpoint\":\"\"}]"))

(fact "should extract the predicates"
      (-> eskip-json
          (eskip-json-to-clj)
          (predicates))
      => "hello(1, \"hello\", /^.*$/) && hello1()")

(fact "should extract the endpoint"
      (-> eskip-json
          (eskip-json-to-clj)
          (endpoint))
      => "\n   -> \"http://www.hello.com/hello\"")

(fact "should extract the shunt endpoint"
      (-> eskip-json-small
          (eskip-json-to-clj)
          (endpoint))
      => "\n   -> <shunt>")

(fact "should extract the filters"
      (-> eskip-json
          (eskip-json-to-clj)
          (filters))
      => "filter1(1, \"hello\")\n   -> filter2()")

(fact "json->eskip should parse a small map to eskip"
      (json->eskip eskip-json-small) => "hello1: *\n   -> <shunt>;")

(fact "json->eskip should parse a json map to eskip"
      (json->eskip eskip-json) => (str "hello1: hello(1, \"hello\", /^.*$/) && hello1()"
                                       "\n   -> filter1(1, \"hello\")"
                                       "\n   -> filter2()"
                                       "\n   -> \"http://www.hello.com/hello\";"))

(fact "json->eskip should parse a json array to eskip"
      (json->eskip eskip-json-array) => (str "hello1: *"
                                             "\n   -> <shunt>;"
                                             "\n\n"
                                             "hello2: *"
                                             "\n   -> <shunt>;"))
