(ns instaskip.case-utils-facts
  (:require [instaskip.case-utils :refer :all]
            [midje.sweet :refer :all]
            [midje.util :refer [expose-testables]]))

(expose-testables instaskip.case-utils)

(facts "snake-to-hyphen"
       (fact "transforms snake case string to hyphen case string"
             (snake-to-hyphen "hello_world_and_me") => "hello-world-and-me"))

(facts "snake-to-hyphen-keywords"
       (fact "transforms a snake case string into a hyphen case keyword"
             (snake-to-hyphen-keyword "hello_world_and_me") => :hello-world-and-me))
