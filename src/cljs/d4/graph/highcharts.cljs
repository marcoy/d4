(ns d4.graph.highcharts
  (:require [d4.utils :refer [log by-id]]
            [jayq.core :refer [$ css html]])
  (:require-macros [d4.graph.highcharts-macros :refer [highcharts-opts-fn]]))


(defrecord Highcharts [selector options])


(highcharts-opts-fn x-axis :xAxis)
(highcharts-opts-fn y-axis :yAxis)
(highcharts-opts-fn title :title)
(highcharts-opts-fn legend :legend)
(highcharts-opts-fn chart :chart)
(highcharts-opts-fn tooltip :tooltip)

(defn series
  [highcharts series-objs]
  (update-in highcharts [:options :series] concat series-objs))


(defn render
  "Render"
  [highcharts]
  (let [element ($ (:selector highcharts))
        options (clj->js (:options highcharts))]
    (.highcharts element options)))


(defn highcharts
  "Constructor for Highcharts"
  [selector & {:as opts}]
  (if (some? opts)
    (Highcharts. selector opts)
    (Highcharts. selector {})))
