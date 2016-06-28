(ns instaskip.case-utils-facts
  (:require [instaskip.case-utils :refer :all]
            [midje.sweet :refer :all]))

(facts "snake->hyphen"
       (fact "transforms snake case string to hyphen case string"
             (snake->hyphen "hello_world_and_me") => "hello-world-and-me"))

(facts "snake->hyphen-keywords"
       (fact "transforms a snake case string into a hyphen case keyword"
             (snake->hyphen-keyword "hello_world_and_me") => :hello-world-and-me))

(facts "hyphen-keyword->snake"
       (fact "transforms a hyphen keyword into a snake case string"
             (hyphen-keyword->snake :hello-world-and-me) => "hello_world_and_me"))

(facts "hyphen-keyword-map->snake"
       (fact "transforms a map with hyphen keyword keys into a map with snake case strings"
             (hyphen-keyword-map-keys->snake {:hello-world-and-me "world"}) => {"hello_world_and_me" "world"}))