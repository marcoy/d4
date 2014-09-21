(ns d4.graph.highcharts-macros)


(defmacro highcharts-opts-fn [fname opt-key]
  `(defn ~fname [highcharts# & {:as opts#}]
     (update-in highcharts# [:options ~(keyword opt-key)] merge opts#)))
