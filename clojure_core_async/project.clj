(defproject cljs-async "0.0.1-SNAPSHOT"
  :description "core.async Simon"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :source-paths ["src/clj" "src/cljs"]
  :repl-options {:init-ns weasel.repl.websocket ; probably don't want init-ns, just load the lib...
                 :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
  :dependencies [[org.clojure/clojure "1.9.0-alpha13"]
                 [org.clojure/core.async "0.2.395"]
                 [com.cemerick/piggieback "0.2.1"]
                 [weasel "0.7.0" :exclusions [org.clojure/clojurescript]]
                 [org.clojure/clojurescript "1.9.293"]]
  :plugins [[lein-cljsbuild "1.1.4"]]
  :cljsbuild {
    :builds [{
      :source-paths ["src/cljs"]
      :compiler {
                 :output-to "dev-resources/public/js/cljs_async.js"
                 :optimizations :whitespace
                 :pretty-print true }}]})
