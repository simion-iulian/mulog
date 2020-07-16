 (ns com.brunobonacci.mulog.publishers.advanced-console
   (:require [com.brunobonacci.mulog.buffer :as rb]
             [com.brunobonacci.mulog.utils :as ut]
             [clansi.core :as ansi]))

(defn colorize
  [value color]
  (->> value
       (map str)
       (map #(ansi/style % color))))

(defn colorize-item
  [item]
  (let [colorized-keys (colorize (keys item) :green)
        colorized-vals (colorize (vals item) :red)
        colorized-item (zipmap colorized-keys colorized-vals)]
    colorized-item))

(deftype AdvancedConsolePublisher
         [config buffer]
  
  com.brunobonacci.mulog.publisher.PPublisher
  (agent-buffer [_]
    buffer)

  (publish-delay [_]
    200)
  
  (publish [_ buffer]
    (doseq [item (map second (rb/items buffer))
            :let [formatter (config :format)]]
      (cond 
        (= formatter :default-formatter) 
        (println  (colorize-item item))
        
        :default (println item))
      (flush))
    (rb/clear buffer)))

(defn advanced-console-publisher
  [config]
  (AdvancedConsolePublisher. config (rb/agent-buffer 10000)))
