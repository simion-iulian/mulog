 (ns com.brunobonacci.mulog.publishers.advanced-console
   (:require [com.brunobonacci.mulog.buffer :as rb]
             [clansi.core :as ansi]
             ))

(defn ansi-color
  [value color]
  (->> value
       (map str)
       (map #(ansi/style % color))))

(defn colorize
  [item color]
  (ansi-color item color))

(def formatters
  (atom {}))

(defn register-formatters
  [formatter-config]
  (reset! formatters formatter-config))

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
            :let [matching-formatters (-> (:format config)
                                          (collect-formatting-matchers item))]]
      (println "got formatters: " matching-formatters))
    (flush)
    (rb/clear buffer)))

(defn advanced-console-publisher
  [config]
  (AdvancedConsolePublisher. config (rb/agent-buffer 10000)))