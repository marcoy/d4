(ns d4.core
  (:require [clojure.browser.repl]
            [cljs.core.async :as async]
            [d4.timeseries.influxdb :as influxdb]
            [d4.utils :refer [log random-num]]
            [jayq.core :refer [$]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defonce d3 js/d3)
(defonce dimple js/dimple)
(defonce rickshaw js/Rickshaw)
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
  (let [chart-element (.getElementById js/document "chart")
        y-axis-element (.getElementById js/document "y-axis")
        data (clj->js (for [x (range 3)] {:x x :y (random-num)}))
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
    (.render graph)
    (go-loop [n 3]
      (.push data (clj->js {:x n :y (random-num)}))
      (when (> n 20)
        (.shift data))
      (.update graph)
      (<! (async/timeout 2000))
      (recur (inc n)))))


(defn epoch-example
  []
  (let [chart-elem ($ :#chart)
        data (clj->js [{:label "Series 1"
                        :values [{:time (/ (-> (js/Date.) (.getTime)) 1000)}]}])]
    (doto chart-elem
      (.width "800px")
      (.height "350px"))
    (.epoch chart-elem (clj->js {:type "time.line" :data data
                                 :queueSize 200 :pixelRatio 1
                                 :axes ["bottom" "left" "right"]}))))


(defn main
  []
  (let [ep-graph (epoch-example)
        influxdb (influxdb/connect :database "d4")
        f (fn [series-data]
            (let [points (get (first series-data) "points")
                  ep-points (clj->js (map (fn [point]
                                            {:time (/ (get point "time") 1000)
                                             :y (get point "value")})
                                          points))]
              (doall (for [p ep-points]
                       (.push ep-graph (array p))))))]
    (log d3)
    (log dimple)
    (log rickshaw)
    (set! graph ep-graph)
    (influxdb/generate-values influxdb "sample" :interval 2000)
    (influxdb/influxdb-stream influxdb "sample" f :poll-interval 6000)))


(set! (.-onload js/window) main)
