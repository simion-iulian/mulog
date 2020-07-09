(ns advanced-console-test
  (:require [com.brunobonacci.mulog :as mu]))

;; starting the publisher
(def publisher (mu/start-publisher! {:type :advanced-console}))

;;printing with ansi and overriding default values
(mu/log :test :pairs-test "pairs of color")

;; (publisher)

;; the log function seems to use pr internally
