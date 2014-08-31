(ns d4.graph.rickshaw
  (:require-macros [cljs.core.match.macros :refer [match]])
  (:require [cljs.core.match]
            [d4.utils :refer [log by-id]]))


(defonce Rickshaw js/Rickshaw)


(defn create-graph
  [element-str & {:keys [width length series] :as props}]
  (let [elem (by-id element-str)]))
