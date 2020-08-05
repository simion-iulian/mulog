(ns advanced-console-test
  (:require [com.brunobonacci.mulog :as mu]
            [where.core :refer [where]]
            [com.brunobonacci.mulog.publishers.advanced-console :refer [register-formatters]]))

(def formatting 
  [(where [:mulog/event-name = :green-test])
   :red-green

   (where [:mulog/event-name = :blue-test])
   :blue-yellow
   
   (where [:mulog/event-name = :http-test])
   :yellow-cyan
   
   :default-formatter :magenta-cyan])

;; example formats
(def pair-format
  {:type :pairs
   :red-green [:red :green]})

(def entry-format
  {:type :entry
   :http-error [:yellow :blue]})

(def event-format
  {:type :event
   :event-test :magenta})

(register-formatters {:red-green    {:keys-color :green
                                     :vals-color :red}
                      :blue-yellow  {:keys-color :magenta
                                     :vals-color :cyan}
                      :magenta-cyan {:keys-color :yellow
                                     :vals-color :blue}
                      :yellow-cyan {:keys-color :yellow
                                    :vals-color :cyan}})

(def publishers (mu/start-publisher!
                   {:type :advanced-console
                    :format formatting}))

(do
  (mu/log :green-test :pairs-test "pairs of color")
  (mu/log :blue-test :pairs-test "pairs of color")
  (mu/log :default-test :pairs-test "pairs of color"))

(mu/log :http-test :http-error 404)
;;  (publishers)

;; the log function seems to use pr internally
