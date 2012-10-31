(ns parallel-consumers.test.core
  (:use [parallel-consumers.core])
  (:use [clojure.test]))

(deftest test-queue
  (do
    (add! global-queue "Test string")
    (is (= "Test string" (take! global-queue)))
    ))