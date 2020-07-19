 (ns com.brunobonacci.mulog.publishers.advanced-console
   (:require [com.brunobonacci.mulog.buffer :as rb]
             [where.core :refer [where]]
             [clansi.core :as ansi]))


(def users 
  [{:id 12
    :name "klk"}
   {:id 13
    :name "ilk"}
   {:id 14
    :name "elk"}])

(filter (where :id > 12) users)

(defn ansi-color
  [value color]
  (->> value
       (map str)
       (map #(ansi/style % color))))

(defn colorize
  [item config]
  (let [colorized-keys (ansi-color (keys item) (config :keys-color))
        colorized-vals (ansi-color (vals item) (config :vals-color))
        colorized-item (zipmap colorized-keys colorized-vals)]
    colorized-item))

(def green-red
  {:keys-color :green
   :vals-color :red})

(def blue-yellow
  {:keys-color :yellow
   :vals-color :blue})

(def formatters 
  {:red-green green-red
   :blue-yellow blue-yellow})

(deftype AdvancedConsolePublisher
         [config buffer]

  com.brunobonacci.mulog.publisher.PPublisher
  (agent-buffer [_]
    buffer)

  (publish-delay [_]
    200)

  (publish [_ buffer]
           (let [matchers-formatters (->> (:format config)
                                          (partition 2))]
             (doseq [item (map second (rb/items buffer))]
               (doseq [[m f] matchers-formatters]
                 (when (m item)
                   (println (colorize item (f formatters)))))))
           (flush)
           (rb/clear buffer)))

(defn advanced-console-publisher
  [config]
  (AdvancedConsolePublisher. config (rb/agent-buffer 10000)))
