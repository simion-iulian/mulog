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

(def formatters
  (atom {}))

(defn register-formatters
  [formatter-config]
  (reset! formatters formatter-config))

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

;; I want to add another formatter 

(deftype AdvancedConsolePublisher
         [config buffer]
  com.brunobonacci.mulog.publisher.PPublisher
  (agent-buffer [_]
    buffer)

  (publish-delay [_]
    200)

  (publish [_ buffer]
    (doseq [item (map second (rb/items buffer))
            :let [formatter (-> (:format config)
                          (find-matching-formatter item))]]
      (if-let [colors (formatter @formatters)]
        (println (colorize item colors))
        (println item)))
    (flush)
    (rb/clear buffer)))

(defn advanced-console-publisher
  [config]
  (AdvancedConsolePublisher. config (rb/agent-buffer 10000)))
