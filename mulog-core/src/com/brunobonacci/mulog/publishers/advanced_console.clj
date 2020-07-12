 (ns com.brunobonacci.mulog.publishers.advanced-console
   (:require [com.brunobonacci.mulog.buffer :as rb]
             [com.brunobonacci.mulog.utils :as ut]
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
      (println colorized-item)
      #_(if (:pretty? config)
          (printf "%s" (ut/pprint-event-str item))
          (printf "%s" (ut/edn-str item)))
      (flush))
    (rb/clear buffer)))

(defn advanced-console-publisher
  [config]
  (AdvancedConsolePublisher. config (rb/agent-buffer 10000)))
