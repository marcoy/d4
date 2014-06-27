(ns server
  (:require [cemerick.austin.repls :refer [browser-connected-repl-js]]
            [clojure.java.io :as io]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources]]
            [net.cgrand.enlive-html :as enlive :refer [deftemplate]]
            [compojure.core :refer [defroutes GET]]))

(defn repl-page []
  (apply str (enlive/emit*
               (enlive/html [:html [:body
                                    [:script {:src "js/d4/main.dev.js"
                                              :type "application/javascript"}]
                                    [:script (browser-connected-repl-js)]]]))))


(deftemplate repl-temp "public/index.html" []
  [:body] (enlive/append
            (enlive/html [:script (browser-connected-repl-js)])))


(defroutes d4-routes
  (GET "/repl-basic" req (repl-page))
  (GET "/repl" req (repl-temp))
  (resources "/"))


(def d4-app
  (-> #'d4-routes
      site))
