 (ns com.brunobonacci.mulog.publishers.advanced-console
   (:require [com.brunobonacci.mulog.buffer :as rb]
             [clojure.pprint :refer [pprint]]))

(deftype AdvancedConsolePublisher
         [config buffer]

  com.brunobonacci.mulog.publisher.PPublisher
  (agent-buffer [_]
    buffer)

  (publish-delay [_]
    500)

  (publish [_ buffer]
    ;; check our printer option
    (let [printer (if (:pretty-print config) pprint prn)]
      ;; items are pairs [offset <item>]
      (doseq [item (map second (rb/items buffer))]
        ;; print the item
        (printer item)))
    ;; return the buffer minus the published elements
    (rb/clear buffer)))

(defn advanced-console-publisher
  [config]
  (MyCustomPublisher. config (rb/agent-buffer 10000)))