(ns d4.timeseries.influxdb
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async :refer [>! alts! chan close! timeout]]
            [d4.utils :refer [log]]
            [goog.string :as gstring]
            [goog.string.format]))


(defonce InfluxDB js/InfluxDB)


(defn connect
  "Create a connection to influxdb db"
  [{:keys [hostname port username password database]
    :or {hostname "localhost" port 8086 username "root" password "root" database ""}}]
  (InfluxDB. #js {:host hostname
                  :port port
                  :username username
                  :password password
                  :database database}))


(defn format-date
  "Format a JS Date() to what InfluxDB recognizes"
  [jsdate]
  (let [year (str (.getFullYear jsdate))
        month (gstring/format "%02d" (+ (.getMonth jsdate) 1))
        day (gstring/format "%02d" (.getDate jsdate))
        hours (gstring/format "%02d" (.getHours jsdate))
        minutes (gstring/format "%02d" (.getMinutes jsdate))
        seconds (gstring/format "%02d" (.getSeconds jsdate))
        millis (gstring/format "%03d" (.getMilliseconds jsdate))]
    (gstring/format "%s-%s-%s %s:%s:%s.%s"
                    year
                    month
                    day
                    hours
                    minutes
                    seconds
                    millis)))


(defn query
  [influxdb query]
  (let [c (chan)]
    (.query influxdb query (fn [data]
                             (go (>! c data)
                                 (close! c))))
    c))


(defn create-stream
  [influxdb series-name poll-interval]
  (let [c (chan)]
    (go-loop [time-cond "time > now() - 5h"]
      (let [select-query (apply str "select * from " series-name " where " time-cond)
            query-chan (query influxdb select-query)
            query-timeout-chan (timeout 5000)
            next-time (format-date (js/Date.))
            [v ch] (alts! [query-timeout-chan query-chan])]
        (log select-query)
        (condp = ch
          ; timed out; recur
          query-timeout-chan (do (log "time-out-chan")
                                 (recur (gstring/format "time > '%s'" next-time)))
          ; got results; try to write it out
          query-chan (if-not (nil? v)
                       (if (>! c v)
                         (do (<! (timeout poll-interval))
                             (recur (gstring/format "time > '%s'" next-time)))
                         (log "Channel closed. Not recurring"))
                       (do (log "Query channel is closed")
                           (<! (timeout poll-interval))
                           (recur (gstring/format "time > '%s'" next-time)))))))
    c))


(defn write-point
  "Write a row to the specified series"
  [influxdb series-name value]
  (.writePoint influxdb
               series-name
               (clj->js value)))


(defn list-series
  [influxdb]
  (let [c (chan)]
    (.query influxdb "select * from /.*/ limit 1" (fn [data]
                                                    (go (>! c data))))
    (async/map< js->clj c)))


(defn test-connection
  "Attempt to connect to influxdb with 5s timeout"
  [influxdb]
  (go (let [list-chan (list-series influxdb)
            [_ ch] (alts! [list-chan (timeout 5000)])]
        (= ch list-chan))))
