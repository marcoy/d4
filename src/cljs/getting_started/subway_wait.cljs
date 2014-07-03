(ns getting-started.subway-wait
  (:require [getting-started.core :refer [d3 dom-rm log]]))

;;
;; Chapter 4 - Interaction and Transitions
;;
(def time-scale nil)
(def percent-scale nil)


(defn add-label
  [circle d g]
  (-> d3
      (.select circle)
      (.transition)
      (.attr "r" 9))
  (-> g
      (.append "text")
      (.text (aget (.split (.-line_id d) "_") 1))
      (.attr "x" (time-scale (.-time d)))
      (.attr "y" (percent-scale (.-late_percent d)))
      (.attr "dy" "0.35em")
      (.attr "dx" "-0.30em")
      (.attr "class" "linelabel")
      (.style "opacity" 0)
      (.style "fill" "white")
      (.transition)
        (.style "opacity" 1)))


(defn draw-timeseries
  "Draw timeseries"
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
              (.attr "class" (aget (.split id "_") 1)))
        enter-duration 1000]
    (-> g
        (.append "path")
        (.attr "d" (line data)))
    ; add circles to represent the data points
    (-> g
        (.selectAll "circle")
        (.data data)
        (.enter)
        (.append "circle")
          (.attr "cx" (fn [d] (time-scale (.-time d))))
          (.attr "cy" (fn [d] (percent-scale (.-late_percent d))))
          (.attr "r" 0))
    (-> g
        (.selectAll "circle")
        (.transition)
        (.delay (fn [d i] (* (/ i (.-length data)) enter-duration)))
        (.attr "r" 5)
        (.each "end" (fn [d i] (if (= i (- (.-length data) 1))
                                 (this-as c (add-label c d g))))))
    ; interactivity
    (-> g
        (.selectAll "circle")
        (.on "mouseover" (fn [d] (this-as e (-> d3
                                                (.select e)
                                                (.transition)
                                                (.duration 10)
                                                (.attr "r" 9)))))
        (.on "mouseout" (fn [d i] (this-as e
                                  (if-not (= i (- (.-length data) 1))
                                    (-> d3
                                        (.select e)
                                        (.transition)
                                        (.attr "r" 5)))))))
    (-> g
        (.selectAll "circle")
        (.on "mouseover.tooltip" (fn [d]
                                   (-> d3 (.select (str "text#" (.-line_id d))) (.remove))
                                   (-> d3
                                       (.select "#chart")
                                       (.append "text")
                                       (.text (str (.-late_percent d) "%"))
                                       (.attr "x" (+ (time-scale (.-time d)) 10))
                                       (.attr "y" (- (percent-scale (.-late_percent d)) 10))
                                       (.attr "id" (.-line_id d))))))
    (-> g
        (.selectAll "circle")
        (.on "mouseout.tooltip" (fn [d]
                                  (-> d3
                                      (.select (str "text#" (.-line_id d)))
                                      (.transition)
                                      (.duration 500)
                                      (.style "opacity" 0)
                                      (.attr "transform" "translate(10,-10)")
                                      (.remove)))))))


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

(subway-wait-mean)
