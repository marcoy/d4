(ns d4.core
  (:require [clojure.browser.repl]
            [cljs.core.async :as async]
            [d4.graph.highcharts :as hc]
            [d4.graph.rickshaw :refer [Rickshaw] :as rickshaw]
            [d4.timeseries.influxdb :as influxdb]
            [d4.utils :refer [by-id current-millis log random-num]]
            [jayq.core :refer [$ css html]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defonce d3 js/d3)
(defonce dimple js/dimple)
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
        data (clj->js [])
        fixed-series (Rickshaw.Series.FixedDuration.
                       (array)
                       js/undefined
                       (clj->js
                         {:timeInterval 10
                          :maxDataPoints 100}))
        graph-props (clj->js {:element chart-element
                              :width 900
                              :length 500
                              :preserve true
                              :stroke true
                              ; :series [{:color "steelblue"
                              ;           :data data}]
                              :renderer "line"
                              :series fixed-series})
        rs-graph (Rickshaw.Graph. graph-props)
        x-axis (Rickshaw.Graph.Axis.Time. (js-obj "graph" rs-graph
                                                  "timeFixture" (Rickshaw.Fixtures.Time.Local.)))
        y-axis (Rickshaw.Graph.Axis.Y. (clj->js {:graph rs-graph
                                                 :tickFormat (.-formatKMBT Rickshaw.Fixtures.Number)}))
        hover (Rickshaw.Graph.HoverDetail. (js-obj "graph" rs-graph))]
    (set! graph rs-graph)
    (log rs-graph)
    (.render x-axis)
    (.render y-axis)
    (.render rs-graph)
    (go-loop [n 0]
      (let [series fixed-series
            r-num (random-num)
            millis (current-millis)]
        ; (log (str "Num: " r-num ". Millis: " millis))
        (.addData series (js-obj "foo" r-num) millis)
        (.update rs-graph)
        (<! (async/timeout 3000))
        (recur (inc n))))))


(defn sine [n]
  (for [x (range n)]
    [x (.sin js/Math (/ x 10))]))


(defn consine [n]
  (for [x (range n)]
    [x (.cos js/Math (/ x 10))]))


(defn highcharts-example []
  (let [container ($ "#chart_container")
        chart-type {:chart {:type "line"}}
        title {:tilte {:text "Fruit Consumption"}}
        x-axis {:xAxis {:categories ["Apples" "Bananas" "Oranges"]}}
        y-axis {:yAxis {:title  {:text "Fruit eaten"}}}
        series {:series [{:name "Jane" :data [1 2 4] :type "line"}
                         {:name "John" :data [5 7 3] :type "line"}]}
        config (merge chart-type title x-axis y-axis series)]
    ; (.highcharts container (clj->js config))
    (-> (hc/highcharts "#chart_container")
        (hc/title :text "Sine and Cosine")
        (hc/chart :type "line"
                  :zoomType "x")
        (hc/series [{:data (sine 200) :name "sine" :type "column"}
                    {:data (consine 200) :name "cosine"}]))))


(defn dynamic-highcharts []
  (let [chart (-> (hc/highcharts "#chart_container")
                  (hc/title :text "Dynamic Chart")
                  (hc/series [{:data [] :name "first"}
                              ; {:data [4 5 6] :name "second"}
                              ]))
        hc-chart (hc/render chart)]
    hc-chart))


(defn main []
  (let [hc-chart (dynamic-highcharts)
        first-series (hc/get-series hc-chart "first")]
    (log first-series)
    (go-loop []
      (<! (async/timeout 1000))
      (hc/push-point first-series (random-num) :max-points 20)
      (recur))))


(set! (.-onload js/window) main)
