(ns d4.repl
  (:require [clojure.core.async :as async]
            [clojure.tools.namespace.repl :refer [refresh]]
            [net.cgrand.enlive-html :as enlive]
            [org.httpkit.client :as http]
            [org.httpkit.server :refer [run-server]]
            [ring.middleware.reload :as reload]
            [server :refer [d4-app]]))

(defn brepl []
  (cemerick.austin.repls/cljs-repl
    (reset! cemerick.austin.repls/browser-repl-env
            (cemerick.austin/repl-env))))

(defn run []
  (defonce ^:private server
    (run-server (reload/wrap-reload #'d4-app) {:port 3000 :join? false}))
  server)
