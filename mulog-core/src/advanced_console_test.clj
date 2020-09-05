(ns advanced-console-test
  (:require [com.brunobonacci.mulog :as mu]
            [where.core :refer [where]]
            [com.brunobonacci.mulog.publishers.advanced-console :as advanced-console]))

(def format-rules
  [(where :mulog/event-name :is? :line-test)
   {:line-test :event-format}

   (where :mulog/event-name :is? :http-test)
   {:http-test :http-format}

   (where contains? :http-error)
   {:http-error :http-error-format}

   :default-formatter [:magenta :underline]])

(advanced-console/register-formatters
 {:event-format      {:event :green}
  :http-format       {:event :yellow}
  :http-error-format {:pair :cyan}})

(def publishers
  (mu/start-publisher!
                 {:type :advanced-console
                  :format format-rules}))

(do
  (mu/log :line-test :whole-line-test "whole line should be colored")
  (mu/log :http-test :whole-line-but-different "whole line should be colored but in a different way")
  (mu/log :default-test :defaults "this should use the default"))

(mu/log
 :http-test
 :http-error 404
 :more-stuff-in-the-event "should be colored like 
                           the rest of the line")

;; (publishers)

