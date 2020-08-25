 (ns com.brunobonacci.mulog.publishers.advanced-console
   (:require [com.brunobonacci.mulog.buffer :as rb]
             [clansi.core :as ansi]
             ))

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
(defn match-formatters [entry rules]
  (->> rules
       (partition 2)
       (map
        (fn [[match? fmt]]
          (when (match? (apply hash-map entry))
            (let [rule-fmt (-> fmt vals first)
                  rule-key (-> fmt keys first)
                  formatter (@formatters rule-fmt)
                  colorized-pairs (if (rule-fmt contains? :pair)
                                    (colorize entry (:pair formatter))
                                    entry)]
              (apply hash-map colorized-pairs)))))
       (into {})))

(defn collect-formatting-matchers
  [rules item]
  (->> item
       (mapcat
        (fn [entry]
          (match-formatters entry rules)))
       (remove nil?)
       ))

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
            :let [matching-formatters (-> (:format config)
                                          (collect-formatting-matchers item))]]
      (println "got formatters: " matching-formatters)
      #_(if-let [colors (matching-formatters @formatters)]
        (println (colorize item colors))
        (println item))
      ;; (println)
      )
    (flush)
    (rb/clear buffer)))

(defn advanced-console-publisher
  [config]
  (AdvancedConsolePublisher. config (rb/agent-buffer 10000)))