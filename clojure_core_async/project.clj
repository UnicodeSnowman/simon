(defproject cljs-async "0.0.1-SNAPSHOT"
  :description "core.async Simon"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :source-paths ["src/clj" "src/cljs"]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.395"]
                 [org.clojure/clojurescript "1.9.293"]]
  :plugins [[lein-cljsbuild "1.1.4"]]
  :cljsbuild {
    :builds [{
      :source-paths ["src/cljs"]
      :compiler {
                 :output-to "dev-resources/public/js/cljs_async.js"
                 :optimizations :whitespace
                 :pretty-print true }}]})
