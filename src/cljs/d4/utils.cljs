(ns d4.utils)


(defn log
  [x]
  (.log js/console x))


(defn random-num
  "Return a random number between 1 and 100 (inclusive)"
  []
  (.floor js/Math (+ (* (.random js/Math) 100) 1)))


(defn tc
  "Helper function to test brepl connection"
  []
  (js/alert "Connected"))
