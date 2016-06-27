(ns instaskip.eskip-facts
  (:require [instaskip.eskip :refer :all]
            [midje.sweet :refer :all]))


(def simple-eskip-routes (str "hello1: *"
                              "\n   -> <shunt>;"
                              "\n\n"
                              "hello2: *"
                              "\n   -> <shunt>;"))

(def eskip-routes (str "hello1: hello(1, \"hello\", /^.*$/, 1.5) && hello1()"
                       "\n   -> filter1(1, \"hello\", 1.3)"
                       "\n   -> filter2()"
                       "\n   -> \"http://www.hello.com/hello\";"))

(fact "should transform simple routes to json and back to eskip"
      (-> simple-eskip-routes
          eskip->json
          json->eskip)
      => simple-eskip-routes)


(fact "should transform to json and back to eskip"
      (-> eskip-routes
          eskip->json
          json->eskip)
      => eskip-routes)
