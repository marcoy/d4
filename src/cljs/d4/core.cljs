(ns d4.core
  (:require [clojure.browser.repl]
            [cljs.core.async :as async]
            [d4.timeseries.influxdb :as influxdb]
            [d4.utils :refer [log]])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(enable-console-print!)

(defonce d3 js/d3)
(defonce nv js/nv)
(defonce dimple js/dimple)
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


;;;
;;;
;;;
(defn random-num
  []
  (.floor js/Math (+ (* (.random js/Math) 100) 1)))


(defn run-generator
  []
  (let [influxdb (influxdb/connect {:database "d4"})]
    (go-loop [n (random-num)]
      (influxdb/write-point influxdb "sample" {:value n})
      (log (str "Wrote: " n))
      (<! (async/timeout 2000))
      (recur (random-num)))))


(defn main
  []
  (let [influxdb (influxdb/connect {:database "d4"})
        stream (influxdb/create-stream influxdb "sample" "30m" 5000)]
    (log d3)
    (log nv)
    (log dimple)
    (run-generator)
    (go-loop []
      (let [values (<! stream)]
        (print values)
        (log values)
        (recur)))
    ))


(set! (.-onload js/window) main)
