(ns instaskip.core-test
  (:require [clojure.test :refer :all]
            [instaskip.core :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 1))))


(deftest eskip-test
  (testing "Some route is parsed"
    (is (= (eskip "hello: predicate1() && predicate2(\"arg1\", 4.3) -> filter1(\"arg1\") -> filter2(\"arg1\", 4.3, \"arg2\") -> filter3() -> backend") []))))
