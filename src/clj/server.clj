(ns server
  (:require [cemerick.austin.repls :refer [browser-connected-repl-js]]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources]]
            [net.cgrand.enlive-html :as enlive]
            [compojure.core :refer [defroutes GET]]))

(defn repl-page []
  (apply str (enlive/emit*
               (enlive/html [:html [:body
                                    [:script {:src "js/d4/main.dev.js"
                                              :type "application/javascript"}]
                                    [:script (browser-connected-repl-js)]]]))))

(defroutes d4-routes
  (GET "/repl" req (repl-page))
  (resources "/"))

(def d4-app
  (-> #'d4-routes
      site))
