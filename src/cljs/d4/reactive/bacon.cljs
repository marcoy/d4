(ns d4.reactive.bacon
  (:require [d4.utils :refer [log]]
            [jayq.core :refer [$] :as jq]))


(defonce Bacon js/Bacon)


(let [stream (.fromPoll Bacon 1000 #(Bacon.Next. (js/Date.)))]
  ; (.onValue stream (fn [d] (log d)))
  )
