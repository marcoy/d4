(ns getting-started.core)


(defonce d3 js/d3)


(defn dom-rm
  "Remove an element from the DOM"
  [selector]
  (-> d3
      (.selectAll selector)
      (.remove)))


(defn log
  "Log to console"
  [x]
  (.log js/console x))


;;
;; Service status
;;
(defn service-status-draw
  [data]
  (-> d3
      (.select "body")
      (.append "ul")
      (.selectAll "li")
      (.data data)
      (.enter)
      (.append "li")
      (.text (fn [d]
               (str (aget d "name") ": " (aget d "status")))))
  (-> d3
      (.selectAll "li")
      (.style "font-weight" (fn [d]
                              (if (= "GOOD SERVICE" (aget (.-status d) 0))
                                "normal"
                                "bold")))))

(defn service-status []
  (-> d3 (.json "data/service_status.json" service-status-draw)))


;;
;; Graphing Mean Daily Plaza Traffic
;;
(defn mean-daily-plaza-draw
  [data]
  (-> d3
      (.select "body")
      (.append "div")
      (.attr "class" "chart")
      (.selectAll ".bar")
      (.data (.-cash data))
      (.enter)
      (.append "div")
        (.attr "class" "bar")
        (.style "width" (fn [d] (str (/ (.-count d) 100) "px")))
        (.style "outline" "1px solid black")
        (.text (fn [d] (.round js/Math (.-count d))))))

(defn mean-daily-plaza
  "Graphing Mean Daily Plaza Traffic"
  []
  (-> d3 (.json "data/plaza_traffic.json" mean-daily-plaza-draw)))


;;
;; Bus Breakdown, Accident, and Injury
;;
(defn bus-breakdown-draw
  [data]
  (let [margin 50
        width  700
        height 300
        x-extent (.extent d3 data (fn [d] (.-collision_with_injury d)))
        x-scale (-> d3.scale
                    (.linear)
                    (.range #js [margin (- width margin)])
                    (.domain x-extent))
        y-extent (.extent d3 data (fn [d] (.-dist_between_fail d)))
        y-scale (-> d3.scale
                    (.linear)
                    (.range #js [(- height margin) margin])
                    (.domain y-extent))
        x-axis (-> d3.svg
                   (.axis)
                   (.scale x-scale))
        y-axis (-> d3.svg
                   (.axis)
                   (.scale y-scale)
                   (.orient "left"))]
    (-> d3
        (.select "body")
        (.append "svg")
        (.attr "width" width)
        (.attr "height" height)
        (.selectAll "circle")
        (.data data)
        (.enter)
        (.append "circle"))
    (-> d3
        (.selectAll "circle")
        (.attr "cx" (fn [d] (x-scale (.-collision_with_injury d))))
        (.attr "cy" (fn [d] (y-scale (.-dist_between_fail d)))))
    (-> d3
        (.selectAll "circle")
        (.attr "r" 5))
    ; x-axis
    (-> d3
        (.select "svg")
        (.append "g")
        (.attr "class" "x axis")
        (.attr "transform" (str "translate(0, " (- height margin) ")"))
        (.call x-axis))
    (-> d3
        (.select ".x.axis")
        (.append "text")
        (.text "collisions with injury (per million miles)")
        (.attr "x" (- (/ width 2) margin))
        (.attr "y" (/ margin 1.5)))
    ; y-axis
    (-> d3
        (.select "svg")
        (.append "g")
        (.attr "class" "y axis")
        (.attr "transform" (str "translate(" margin ", 0)"))
        (.call y-axis))
    (-> d3
        (.select ".y.axis")
        (.append "text")
        (.text "mean distance between failure (miles)")
        (.attr "transform" "rotate (-90, -43, 0) translate(-280)"))))

(defn bus-breakdown
  "Bus Breakdown, Accident, and Injury"
  []
  (-> d3 (.json "data/bus_perf.json" bus-breakdown-draw)))


;;
;; Graphing Turnstile Traffic
;;
(defn turnstile-traffic-draw
  "Graphing Turnstile Traffic (Draw)"
  [data]
  (let [margin 40
        width  (- 700 margin)
        height (- 300 margin)
        count-extent (.extent d3 (.concat (.-times_square data) (.-grand_central data))
                                    (fn [d] (.-count d)))
        count-scale (-> d3.scale
                        (.linear)
                        (.domain count-extent)
                        (.range #js [height margin]))
        count-axis (-> d3.svg
                       (.axis)
                       (.scale count-scale)
                       (.orient "left"))
        time-extent (.extent d3 (.concat (.-times_square data) (.-grand_central data))
                                (fn [d] (.-time d)))
        time-scale (-> (.-time d3)
                       (.scale)
                       (.domain time-extent)
                       (.range #js [margin width]))
        time-axis (-> d3.svg
                      (.axis)
                      (.scale time-scale))
        line (-> d3.svg
                 (.line)
                 (.x (fn [d] (time-scale (.-time d))))
                 (.y (fn [d] (count-scale (.-count d)))))]
    (-> js/d3
        (.select "body")
        (.append "svg")
          (.attr "width" (+ width margin))
          (.attr "height" (+ height margin))
        (.append "g")
          (.attr "class"  "chart"))
    ; times square
    (-> js/d3
        (.select "svg")
        (.selectAll "circle.times_square")
        (.data (.-times_square data))
        (.enter)
        (.append "circle")
          (.attr "class" "times_square"))
    ; grand central
    (-> js/d3
        (.select "svg")
        (.selectAll "circle.grand_central")
        (.data (.-grand_central data))
        (.enter)
        (.append "circle")
          (.attr "class" "grand_central"))
    ; circle attrs
    (-> js/d3
        (.selectAll "circle")
        (.attr "cx" (fn [d] (time-scale (.-time d))))
        (.attr "cy" (fn [d] (count-scale (.-count d))))
        (.attr "r" 3))
    ; axis
    (-> js/d3
        (.select "svg")
        (.append "g")
        (.attr "class" "x axis")
        (.attr "transform" (str "translate(0, " height ")"))
        (.call time-axis))
    (-> js/d3
        (.select "svg")
        (.append "g")
        (.attr "class" "y axis")
        (.attr "transform" (str "translate(" margin ", 0)"))
        (.call count-axis))
    ; line
    (-> js/d3
        (.select "svg")
        (.append "path")
        (.attr "d" (line (.-times_square data)))
        (.attr "class" "times_square"))
    (-> js/d3
        (.select "svg")
        (.append "path")
        (.attr "d" (line (.-grand_central data)))
        (.attr "class" "grand_central"))))

(defn turnstile-traffic
  "Graphing Turnstile Traffic"
  []
  (-> js/d3 (.json "data/turnstile_traffic.json" turnstile-traffic-draw)))
