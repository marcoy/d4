(ns d4.graph.dygraph
  (:require [d4.utils :refer [by-id]]))


(defprotocol DygraphConfig
  (set-data [this data] "Initial data points")
  (set-title [this title])
  (set-x-label [this x-label])
  (set-y-label [this y-label])
  (render [this]))


(defprotocol DygraphData
  (put-point [this point])
  (drop-point [this]))


(defrecord Dygraph [config graph]
  DygraphConfig
  (set-data [this data]
    (update-in this [:config :file] (fn [x] (if (seq x)
                                              (conj x data)
                                              data))))
  (set-title [this title]
    (update-in this [:config :title] (constantly title)))
  (set-x-label [this x-label]
    (update-in this [:config :xlabel] (constantly x-label)))
  (set-y-label [this y-label]
    (update-in this [:config :ylabel] (constantly y-label)))
  (render [this]
    (let [conf (:config this)
          js-dygraph (js/Dygraph. (by-id (:element conf))
                                  (if (seq (:file conf))
                                    (clj->js (:file conf))
                                    #js [[(js/Date.) 0]])
                                  (clj->js conf))]
      (update-in this [:graph] (constantly js-dygraph)))))


(defn build-dygraph
  "Dygraph constructor"
  [element & {:keys [width height]
              :or {width 900 height 600}}]
  (map->Dygraph {:config {:element element
                          :width width
                          :height height}}))
