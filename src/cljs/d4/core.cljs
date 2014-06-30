(ns d4.core
  (:require [clojure.browser.repl]))


(defonce d3 js/d3)
(defonce nv js/nv)
(defonce body (-> js/d3 (.select "body")))


(defn log
  [x]
  (.log js/console x))


(defn tc
  "Helper function to test brepl connection"
  []
  (js/alert "Connected"))


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
                (.attr "width" 600)
                (.attr "height" 600))]
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


(defn main
  []
  (log d3)
  (log nv))


(set! (.-onload js/window) main)
