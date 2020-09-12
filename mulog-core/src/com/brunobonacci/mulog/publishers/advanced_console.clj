 (ns com.brunobonacci.mulog.publishers.advanced-console
   (:require [com.brunobonacci.mulog.buffer :as rb]
             [clansi.core :as ansi]))

(defn ansi-color
  [value color]
  (->> value
       (map str)
       (map #(ansi/style % color))
       ))

(defn colorize
  [item color]
  (ansi-color item color))

(def formatters
  (atom {}))

(defn register-formatters
  [formatter-config]
  (reset! formatters formatter-config))

(defn find-format
  [rules [key val]]
  (->> rules
       (partition 2)
       (keep
        (fn [[match? fmt]]
          (when (match? (hash-map key val))
            fmt)))))

(defn find-all-formats 
  [rules entry]
  (mapcat (partial find-format rules) entry))

(defn entry-format
  [entry rules]
  (->> entry
       (find-all-formats rules)
       (keep
        (fn [fmt]
          (let [rule-extractor (-> fmt vals first)
                rule-fmt (@formatters rule-extractor)]
            (:event rule-fmt))))
       (into [])
       (cons (get-in @formatters [:default-formatter :event]))
       last))

(deftype AdvancedConsolePublisher
         [config buffer]
  com.brunobonacci.mulog.publisher.PPublisher
  (agent-buffer [_]
    buffer)

  (publish-delay [_]
    200)

  (publish [_ buffer]
    (doseq [item (map second (rb/items buffer))
            :let [rules (:format config)
                  event-fmt (entry-format item rules)]]
      (println (apply conj (colorize item event-fmt))))
    (flush)
    (rb/clear buffer)))

(defn advanced-console-publisher
  [config]
  (AdvancedConsolePublisher. config (rb/agent-buffer 10000)))