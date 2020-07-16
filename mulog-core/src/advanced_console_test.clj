(ns advanced-console-test
  (:require [com.brunobonacci.mulog :as mu]))

;; starting the publisher
(def publishers (mu/start-publisher!
                      {:type :multi
                       :publishers
                       [{:type :advanced-console 
                         :format :default-formatter}
                        {:type :advanced-console}]}))

;;printing with ansi and overriding default values
(mu/log :test :pairs-test "pairs of color")

;; (publishers)

;; the log function seems to use pr internally
