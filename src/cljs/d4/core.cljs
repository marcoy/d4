(ns d4.core
  (:require [clojure.browser.repl]
            [cljs.core.async :as async]
            [d4.timeseries.influxdb :as influxdb]
            [d4.utils :refer [log random-num by-id]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defonce d3 js/d3)
(defonce dimple js/dimple)
(defonce Rickshaw js/Rickshaw)
(defonce body (-> js/d3 (.select "body")))
(def graph nil)


(defn sin-cos
  []
  (clj->js [{:key "sin data"
             :color "#ff7f0e"
             :values (for [x (range 100)]
                       {:x x :y (.sin js/Math (/ x 10))})}
            {:key "cos data"
             :color "#2ca02c"
             :values (for [x (range 100)]
                       {:x x :y (.cos js/Math (/ x 10))})}]))


(defn dimple-example
  "Dimple line chart. This requires a .tsv file which can be downloaded from
   https://raw.githubusercontent.com/PMSI-AlignAlytics/dimple/master/data/example_data.tsv"
  []
  (let [svg (-> dimple
                (.newSvg "#chart_container" 590 400))]
    (.tsv d3 "/data/example_data.tsv"
      (fn [d]
        (let [Chart (.-chart dimple)
              data (.filterData dimple d "Owner" #js ["Aperture" "Black Mesa"])
              myChart (Chart. svg data)
              _ (.setBounds myChart 60 30 505 305)
              x (.addCategoryAxis myChart "x" "Month")]
          (.addOrderRule x "Date")
          (.addMeasureAxis myChart "y" "Unit Sales")
          (.addSeries myChart "Channel" (-> js/dimple (.-plot) (.-line)))
          (.draw myChart 1000))))))


(defn rickshaw-example
  []
  (let [chart-element (by-id "chart")
        y-axis-element (.getElementById js/document "y-axis")
        data (clj->js [])
        graph-props (clj->js {:element chart-element
                              :width 580
                              :length 250
                              :series [{:color "steelblue"
                                        :data data}]})
        rs-graph (Rickshaw.Graph. graph-props)
        x-axis (Rickshaw.Graph.Axis.Time. (clj->js {:graph rs-graph}))
        y-axis (Rickshaw.Graph.Axis.Y. (clj->js {:graph rs-graph
                                                 :tickFormat (.-formatKMBT Rickshaw.Fixtures.Number)}))]
    (set! graph rs-graph)
    (log rs-graph)
    (.render rs-graph)
    (.render x-axis)
    (.render y-axis)
    (go-loop [n 3]
      (.push data (clj->js {:x n :y (random-num)}))
      (when (> n 20)
        (.shift data))
      (.update rs-graph)
      (<! (async/timeout 2000))
      (recur (inc n)))))


(defn main
  []
  (let [series-data (clj->js [[] [] [] []])
        random (Rickshaw.Fixtures.RandomData. 150)]
    (log d3)
    (log dimple)
    (log Rickshaw)
    (rickshaw-example)))


(set! (.-onload js/window) main)
