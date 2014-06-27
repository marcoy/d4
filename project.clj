(defproject d4 "0.1.0-SNAPSHOT"

  :description "Testing the capability of D3 using clojure and clojurescript"

  :url "http://example.com/FIXME"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-ring "0.8.11"]]

  :repl-options {
                 :init-ns d4.repl
                }

  :source-paths ["src/clj" "src/cljs"]

  :test-paths ["test/clj"]

  :dependencies [[compojure "1.1.8"]
                 [enlive "1.1.5"]
                 [http-kit "2.1.18"]
                 [javax.servlet/servlet-api "2.5"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2234"]
                 [org.clojure/tools.namespace "0.2.4"]
                 [ring "1.3.0"]]

  :profiles {
    :dev
    {:plugins [[com.cemerick/austin "0.1.4"]]

     :cljsbuild {
       :builds {
         :dev
         {:source-paths ["src/cljs"]
          :compiler {:output-to "resources/public/js/d4/main.dev.js"
                     :output-dir "resources/public/js/d4"
                     :source-map "resources/public/js/d4/main.dev.js.map"
                     ; :source-map-path ""
                     :optimizations :whitespace
                     :pretty-print true}}

         ; :dev-none
         ; {:source-paths ["src/cljs"]
         ;  :compiler {:output-to "resources/public/js/d4/main.dev-none.js"
         ;             :output-dir "resources/public/js/d4"
         ;             :source-map "resources/public/js/d4/main.dev-none.js.map"
         ;             ; :source-map-path ""
         ;             :optimizations :none
         ;             :pretty-print true}}
         }}
    }

    :prod
    {:cljsbuild {
       :builds {
         :dev
         {:source-paths ["src/cljs"]
          :compiler {:output-to "resources/public/js/d4/main.js"
                     :optimizations :advanced
                     :pretty-print false}}}}
    }
  }

  :main d4.core

  :ring {:handler server/d4-app})
