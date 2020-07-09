 (ns com.brunobonacci.mulog.publishers.advanced-console
   (:require [com.brunobonacci.mulog.buffer :as rb]
             [clansi.core :as ansi]
             [clojure.pprint :refer [pprint]]))

(deftype AdvancedConsolePublisher
  [config buffer]
 
  com.brunobonacci.mulog.publisher.PPublisher
  (agent-buffer [_]
    buffer)

  (publish-delay [_]
    200)
 
  (publish [_ buffer]
    ;; check our printer option
    (let [printer (if (:pretty-print config) pprint prn)]
      ;; items are pairs [offset <item>]
      (doseq [item (map second (rb/items buffer))
              :let [colorized-keys (->> item
                                        keys
                                        (map str)
                                        (map #(ansi/style % :green)))
                    colorized-vals (->> item
                                        vals
                                        (map str)
                                        (map #(ansi/style % :red)))
                    colorized-item (zipmap colorized-keys colorized-vals)]]
        ;; print the item
        (printer colorized-item)))
    ;; return the buffer minus the published elements
    (rb/clear buffer)))

(defn advanced-console-publisher
  [config]
  (AdvancedConsolePublisher. config (rb/agent-buffer 10000)))
