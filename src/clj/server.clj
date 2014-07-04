(ns server
  (:require [cemerick.austin.repls :refer [browser-connected-repl-js]]
            [clojure.java.io :as io]
            [compojure.handler :refer [site]]
            [compojure.route :refer [resources]]
            [environ.core :refer [env]]
            [net.cgrand.enlive-html :as enlive :refer [deftemplate]]
            [compojure.core :refer [defroutes GET]]))


(defn script
  [content]
  [:script {:type "application/javascript" :charset "utf-8"}
           (str content)])


(defn scriptsrc
  [src]
  [:script {:type "application/javascript" :charset "utf-8" :src (str src)}])


(defn css
  [css]
  [:link {:charset "utf-8" :type "text/css" :rel "stylesheet" :href (str css)}])


(defn repl-page []
  (enlive/emit*
    (enlive/html
      [:html [:body
              [:script {:src "js/d4/main.dev.js"
                        :type "application/javascript"}]
              [:script (browser-connected-repl-js)]]])))


(deftemplate index-template "public/index.html" []
  [:body] (enlive/append
            (condp = (:profile env)
              :dev (enlive/html
                     (scriptsrc "js/d4/goog/base.js")
                     (scriptsrc "js/d4/main.dev.js")
                     (script "goog.require('d4.core')")
                     (script (browser-connected-repl-js)))
              :pre (enlive/html
                     (scriptsrc "js/d4/main.pre.js")
                     (script (browser-connected-repl-js)))
              :prod (enlive/html (scriptsrc "js/d4/main.js")))))


(defn append
  [tags element]
  (let [profile (:profile env)
        elements (apply enlive/html (get-in tags (map keyword [profile element])))]
    (enlive/append elements)))


(deftemplate parametric-template "public/index.html" [tags]
  [:head] (append tags :css)
  [:body] (append tags :js))


(defroutes d4-routes
  (GET "/" req (parametric-template
                 {:dev {:js [(scriptsrc "js/d4/goog/base.js")
                             (scriptsrc "/js/d4/main.dev.js")
                             (script "goog.require('d4.core')")
                             (script (browser-connected-repl-js))]}
                  :pre {:js [(scriptsrc "js/d4/main.pre.js")
                             (script (browser-connected-repl-js))]}
                  :prod {:js [(scriptsrc "js/d4/main.js")]}}))
  (resources "/"))


(def d4-app
  (-> #'d4-routes
      site))
