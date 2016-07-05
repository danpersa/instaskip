(ns instaskip.cats-match
  (:import (cats.monad.exception Success)
           (cats.monad.exception Failure)))

(extend-type Success
  clojure.core.match.protocols/IMatchLookup
  (val-at [this k not-found]
    (case k
      :success (.v this)
      not-found)))

(extend-type Failure
  clojure.core.match.protocols/IMatchLookup
  (val-at [this k not-found]
    (case k
      :failure (.e this)
      not-found)))
