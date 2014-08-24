(ns d4.core
  (:require [clojure.browser.repl]
            [cljs.core.async :as async]
            [d4.timeseries.influxdb :as influxdb]
            [d4.utils :refer [log random-num]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defonce d3 js/d3)
(defonce nv js/nv)
(defonce dimple js/dimple)
(defonce rickshaw js/Rickshaw)
(defonce body (-> js/d3 (.select "body")))


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


(defn nv-example
  []
  (let [svg (-> body
                (.append "svg")
                (.attr "width" 300)
                (.attr "height" 300))]
    (.addGraph nv
               (fn []
                 (let [chart (-> nv.models
                                 (.lineChart)
                                 (.margin #js {:left 100})
                                 (.useInteractiveGuideline true)
                                 (.transitionDuration 350)
                                 (.showLegend true)
                                 (.showYAxis true)
                                 (.showYAxis true))]
                   (-> (.-xAxis chart)
                       (.axisLabel "Time (ms)")
                       (.tickFormat (.format d3 ",r")))
                   (-> (.-yAxis chart)
                       (.axisLabel "Voltage (v)")
                       (.tickFormat (.format d3 ".02f")))
                   ; render
                   (-> svg
                       (.datum (sin-cos))
                       (.call chart))
                   chart)))))


(defn dimple-example
  "Dimple line chart. This requires a .tsv file which can be downloaded from
   https://raw.githubusercontent.com/PMSI-AlignAlytics/dimple/master/data/example_data.tsv"
  []
  ; create a container
  (-> body
      (.append "div")
      (.attr "id" "chartContainer"))
  (let [svg (-> dimple
                (.newSvg "#chartContainer" 590 400))]
    (.tsv d3 "/data/example_data.tsv"
      (fn [d]
        (let [Chart (.-chart dimple)
              data (.filterData dimple d "Owner" #js ["Aperture" "Black Mesa"])
              myChart (Chart. svg data)
              x (.addCategoryAxis myChart "x" "Month")]
          (.addOrderRule x "Date")
          ; (.setBounds myChart 60 30 505 305)
          (.addMeasureAxis myChart "y" "Unit Sales")
          (.addSeries myChart nil (-> js/dimple (.-plot) (.-line)))
          (.draw myChart))))))


(defn influxdb-stream
  []
  (let [influxdb (influxdb/connect {:database "d4"})
        stream (influxdb/create-stream influxdb "sample"
                                       :initial-backfill "30m"
                                       :poll-interval 5000)]
    (influxdb/generate-values influxdb "sample")
    (go-loop []
      (let [values (<! stream)]
        (print values)
        (log values)
        (recur)))))


(defn rickshaw-example
  []
  (let [chart-element (.getElementById js/document "chart")
        y-axis-element (.getElementById js/document "y-axis")
        data (for [x (range 10)] {:x x :y (random-num)})
        graph-props (clj->js {:element chart-element
                              :width 580
                              :length 250
                              :series [{:color "steelblue"
                                        :data data}]})
        graph (rickshaw.Graph. graph-props)
        x-axis (rickshaw.Graph.Axis.Time. #js {:graph graph})
        y-axis (rickshaw.Graph.Axis.Y. #js {:graph graph
                                            :orientation "left"
                                            :tickFormat (.-formatKMBT rickshaw.Fixtures.Number)
                                            :element y-axis-element})]
    (.render graph)))


(defn main
  []
  (let []
    (log d3)
    (log nv)
    (log dimple)
    (log rickshaw)
    (rickshaw-example)))


(set! (.-onload js/window) main)
