(ns advanced-console-test
  (:require [com.brunobonacci.mulog :as mu]
            [where.core :refer [where]]))

(def formatting 
  [(where [:mulog/event-name = :green-test])
   :red-green

   (where [:mulog/event-name = :blue-test])
   :blue-yellow])
;; starting the publisher
(def publishers (mu/start-publisher!
                   {:type :advanced-console
                    :format formatting}))

;;printing with ansi and overriding default values
(do
  (mu/log :green-test :pairs-test "pairs of color")
  (mu/log :blue-test :pairs-test "pairs of color"))

;; (publishers)

;; the log function seems to use pr internally
