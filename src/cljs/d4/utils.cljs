(ns d4.utils)


(defn log
  [x]
  (.log js/console x))


(defn tc
  "Helper function to test brepl connection"
  []
  (js/alert "Connected"))
