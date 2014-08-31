(ns d4.utils)


(defn log
  [x]
  (.log js/console x))


(defn random-num
  "Return a random number between 1 and 100 (inclusive)"
  []
  (.floor js/Math (+ (* (.random js/Math) 100) 1)))


(defn by-id
  [elem-id]
  (.getElementById js/document elem-id))


(defn current-millis []
  (/ (.getTime (js/Date.)) 1000))


(defn tc
  "Helper function to test brepl connection"
  []
  (js/alert "Connected"))
