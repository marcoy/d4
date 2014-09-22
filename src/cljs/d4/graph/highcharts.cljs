(ns d4.graph.highcharts
  (:require [d4.utils :refer [log by-id]]
            [jayq.core :refer [$ css html]])
  (:require-macros [d4.graph.highcharts-macros :refer [highcharts-opts-fn]]))


;; Highcharts object
(defrecord Highcharts [selector options])


;; Functions for Highcharts options
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
    (.highcharts element options)
    (.highcharts element)))


(defn highcharts
  "Constructor for Highcharts"
  [selector & {:as opts}]
  (if (some? opts)
    (Highcharts. selector opts)
    (Highcharts. selector {})))


(defn get-charts
  ([] (.-charts js/Highcharts))
  ([selector] (.highcharts ($ selector))))


(defn get-series
  ([hc-chart] (.-series hc-chart))
  ([hc-chart series-name]
    (let [series (get-series hc-chart)]
      (first (filter #(= (.-name %) series-name) series)))))


(defn push-point
  [series point & {:keys [max-points]}]
  (let [points (.-data series)]
    (if (some? max-points)
      (if (< (count points) max-points)
        (.addPoint series point true false)
        (.addPoint series point true true))
      (.addPoint series point true false))))
