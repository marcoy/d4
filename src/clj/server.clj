(ns server
  (:require [cemerick.austin.repls :refer [browser-connected-repl-js]]
            [clojure.java.io :as io]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources]]
            [environ.core :refer [env]]
            [net.cgrand.enlive-html :as enlive :refer [deftemplate]]
            [compojure.core :refer [defroutes GET]]))


(defn repl-page []
  (enlive/emit*
    (enlive/html
      [:html [:body
              [:script {:src "js/d4/main.dev.js"
                        :type "application/javascript"}]
              [:script (browser-connected-repl-js)]]])))


(defn script
  [content]
  [:script {:type "application/javascript" :charset "utf-8"}
           (str content)])


(defn scriptsrc
  [src]
  [:script {:type "application/javascript" :charset "utf-8" :src (str src)}])


(deftemplate index-template "public/index.html" []
  [:body] (enlive/append
            (condp = (:profile env)
              :dev (enlive/html
                     (scriptsrc "js/d4/goog/base.js")
                     (scriptsrc "js/d4/main.dev.js")
                     (script "goog.require('d4.core')")
                     (script (browser-connected-repl-js)))
              :prod (enlive/html (scriptsrc "js/d4/main.js")))))


(defroutes d4-routes
  (GET "/" req (index-template))
  (resources "/"))


(def d4-app
  (-> #'d4-routes
      site))
