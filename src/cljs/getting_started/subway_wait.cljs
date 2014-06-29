(ns getting-started.subway-wait
  (:require [getting-started.core :refer [d3 dom-rm log]]))

;;
;; Chapter 4 - Interaction and Transitions
;;
(def time-scale nil)
(def percent-scale nil)


(defn draw-timeseries
  [data id]
  (let [line (-> d3.svg
                 (.line)
                 (.x (fn [d] (time-scale (.-time d))))
                 (.y (fn [d] (percent-scale (.-late_percent d))))
                 (.interpolate "linear"))
        g (-> d3
              (.select "#chart")
              (.append "g")
              (.attr "id" (str id "_path"))
              (.attr "class" (aget (.split id "_") 1)))]
    (-> g
        (.append "path")
        (.attr "d" (line data)))))


(defn get-timeseries-data
  []
  (this-as ele
    (let [id (-> d3 (.select ele) (.attr "id"))
          ts (-> d3 (.select (str "#" id "_path")))]
      (if (.empty ts)
        (.json d3 "data/subway_wait.json"
               (fn [data]
                 (-> data
                     (.filter #(= (.-line_id %) id))
                     (draw-timeseries id))))
        (.remove ts)))))


(defn draw
  "Drawing Subway Wait Mean"
  [data]
  (let [container-dimensions #js {:width 900 :height 400}
        margins #js {:top 10 :right 20 :bottom 30 :left 60}
        chart-dimensions #js {:width (- (aget container-dimensions "width")
                                        (aget margins "left")
                                        (aget margins "right"))
                              :height (- (aget container-dimensions "height")
                                         (aget margins "top")
                                         (aget margins "bottom"))}
        chart (-> d3
                  (.select "#timeseries")
                  (.append "svg")
                    (.attr "width" (aget container-dimensions "width"))
                    (.attr "height" (aget container-dimensions "height"))
                  (.append "g")
                    (.attr "transform" (str "translate(" (aget margins "left") ", " (aget margins "top") ")"))
                    (.attr "id" "chart"))
        _ (set! time-scale (-> d3.time
                               (.scale)
                               (.range #js [0 (aget chart-dimensions "width")])
                               (.domain #js [(js/Date. 2009 0 1) (js/Date. 2011 3 1)])))
        _ (set! percent-scale (-> d3.scale
                                  (.linear)
                                  (.range #js [(aget chart-dimensions "height") 0])
                                  (.domain #js [65 90])))
        time-axis (-> d3.svg (.axis) (.scale time-scale))
        count-axis (-> d3.svg (.axis) (.scale percent-scale) (.orient "left"))
        key-items (-> d3
                      (.select "#key")
                      (.selectAll "div")
                      (.data data)
                      (.enter)
                      (.append "div")
                      (.attr "class" "key_line")
                      (.attr "id" (fn [d] (.-line_id d))))]
    ; axes
    (-> chart
        (.append "g")
        (.attr "class" "x axis")
        (.attr "transform" (str "translate(0, " (aget chart-dimensions "height") ")"))
        (.call time-axis))
    (-> chart
        (.append "g")
        (.attr "class" "y axis")
        (.call count-axis))
    (-> d3
        (.select ".y.axis")
        (.append "text")
        (.attr "text-anchor" "middle")
        (.text "percent on time")
        (.attr "transform" "rotate(-270, 0, 0)")
        (.attr "x" (/ (aget container-dimensions "height") 2))
        (.attr "y" 50))
    ; keys
    (-> key-items
        (.append "div")
        (.attr "id" (fn [d] (str "key_square_" (.-line_id d))))
        (.attr "class" "key_square"))
    (-> key-items
        (.append "div")
        (.attr "class" "key_label")
        (.text (fn [d] (.-line_name d))))
    ; interaction
    (-> d3
        (.selectAll ".key_line")
        (.on "click" get-timeseries-data))))


(defn subway-wait-mean
  []
  (.json d3 "data/subway_wait_mean.json" draw))
