(ns d4.timeseries.influxdb
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :as async :refer [>! alts! chan timeout]]
            [d4.utils :refer [log]]))


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
            [v ch] (alts! [list-chan (timeout 5000)])]
        (= ch list-chan))))
