(defproject d4 "0.1.0-SNAPSHOT"

  :description "Testing the capability of D3 using clojure and clojurescript"

  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[lein-cljsbuild "1.0.3"]]

  :repl-options {
                 :init-ns d4.repl
                }

  :source-paths ["src/clj"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2234"]
                 [org.clojure/tools.namespace "0.2.4"]
                 [http-kit "2.1.16"]]

  :profiles {:dev
             {}}

  :main d4.core)
