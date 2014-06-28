(ns d4.core
  (:require [clojure.browser.repl]))


(defonce d3 js/d3)


(defn log
  [x]
  (.log js/console x))


(defn tc
  "Helper function to test brepl connection"
  []
  (js/alert "Connected"))


(defn main
  []
  (log d3))

(set! (.-onload js/window) main)
