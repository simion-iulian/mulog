(ns advanced-console-test
  (:require [com.brunobonacci.mulog :as mu]
            [where.core :refer [where]]
            [com.brunobonacci.mulog.publishers.advanced-console :refer [register-formatters]]))

(def formatting 
  [(where :mulog/event-name :is? :line-test)
   :line-test-event

   (where :mulog/event-name :is :http-test)
   :another-line-event

   (where contains? :http-error)
   :http-error-coloring

   :default-formatter :magenta-red])

(register-formatters {:line-test-event    {:event :green}
                      :another-line-event  {:event :yellow}
                      :http-error-coloring   {:entry [:cyan :underline]}
                      :magenta-red  {:entry {:key :magenta
                                             :value :red}}})

(def publishers (mu/start-publisher!
                   {:type :advanced-console
                    :format formatting}))

(do
  (mu/log :green-test :pairs-test "pairs of color")
  (mu/log :blue-test :pairs-test "pairs of color")
  (mu/log :default-test :pairs-test "pairs of color"))

(mu/log :http-test :http-error 404)

; (publishers)

;; the log function seems to use pr internally
