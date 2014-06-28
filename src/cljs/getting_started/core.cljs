(ns getting-started.core)


(defonce d3 js/d3)


(defn dom-rm
  "Remove an element from the DOM"
  [selector]
  (-> d3
      (.selectAll selector)
      (.remove)))


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
  []
  (-> d3 (.json "data/plaza_traffic.json" mean-daily-plaza-draw)))
