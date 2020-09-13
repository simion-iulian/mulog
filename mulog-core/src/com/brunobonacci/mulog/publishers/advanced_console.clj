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
                  {:keys [pair event]
                   :or   {event (:default-formatter formatters)}}
                  (@formatters rule-fmt)]
              (apply hash-map (colorize entry (or pair event)))))))))

(defn collect-formatting-matchers
  [rules item]
  (->> item
       (mapcat
        (fn [entry]
          (match-formatters entry rules)))
       (remove nil?)
       (apply conj)
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
            :let [rules (:format config)
                  event-fmt (entry-format item rules)
                  pair-formats (pair-formats item rules)
                  pair-keys (keys pair-formats)
                  event-without-pair-fmt (apply dissoc item pair-keys)
                  event-pairs (select-keys item pair-keys)]]
      (println (->> event-pairs
                    (map (fn [[k v]]
                           (colorize-item (hash-map k v)
                                          (get-in pair-formats [k :pair]))))
                    (apply merge 
                           (colorize-item event-without-pair-fmt event-fmt)))))
    (flush)
    (rb/clear buffer)))

(defn advanced-console-publisher
  [config]
  (AdvancedConsolePublisher. config (rb/agent-buffer 10000)))