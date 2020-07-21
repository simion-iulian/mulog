 (ns com.brunobonacci.mulog.publishers.advanced-console
   (:require [com.brunobonacci.mulog.buffer :as rb]
             [clansi.core :as ansi]))

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

(def magenta-cyan
  {:keys-color :magenta
   :vals-color :cyan})

(def blue-yellow
  {:keys-color :yellow
   :vals-color :blue})

(def formatters 
  {:red-green green-red
   :blue-yellow blue-yellow
   :magenta-cyan magenta-cyan})

(defn match-formatter
  [matcher formatter]
  (fn [item]
    (when (matcher item)
      formatter)))

(defn find-matching-formatter
  [matchers-formatters item]
  (let [prepared-matchers (->> matchers-formatters
                               (drop-last 2)
                               (partition 2))
        find-formatter (->> prepared-matchers
                            (map (partial apply match-formatter))
                            (apply some-fn))]
    (if-let [formatter (find-formatter item)]
      formatter
      (:default-formatter (apply hash-map (take-last 2 matchers-formatters))))))

(deftype AdvancedConsolePublisher
  [config buffer]
  com.brunobonacci.mulog.publisher.PPublisher
  (agent-buffer [_]
    buffer)

  (publish-delay [_]
    200)

  (publish [_ buffer]
    (doseq [item (map second (rb/items buffer))
            :let [fmt (-> (:format config)
                          (find-matching-formatter item)
                          formatters)]]
      (println (colorize item fmt)))
    (flush)
    (rb/clear buffer)))

(defn advanced-console-publisher
  [config]
  (AdvancedConsolePublisher. config (rb/agent-buffer 10000)))
