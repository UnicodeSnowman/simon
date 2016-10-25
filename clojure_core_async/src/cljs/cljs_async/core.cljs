;;; If this namespace requires macros, remember that ClojureScript's
;;; macros are written in Clojure and have to be referenced via the
;;; :require-macros directive where the :as keyword is required, while in Clojure is optional. Even
;;; if you can add files containing macros and compile-time only
;;; functions in the :source-paths setting of the :builds, it is
;;; strongly suggested to add them to the leiningen :source-paths.
(ns cljs-async.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async :refer [put! alts! timeout chan]]))

  (defn listen [el evt]
    (let [c (chan)]
      (.addEventListener el evt (fn [e] (put! c e)))
      c))

  (defn listen-query [query evt]
    (let [c (chan)
          els (.from js/Array (.querySelectorAll js/document query))]
      (doseq [el els]
        (.addEventListener el evt (fn [e] (put! c (keyword (-> e .-target .-id))))))
      c))

;  (defn open [e cancel]
;    (go
;      (let [[_ ch] (alts! [cancel (timeout 500)])]
;        (if-not (= ch cancel)
;          (do 
;            (.log js/console "add")
;            (<! cancel)
;            (.log js/console "remove"))
;          (.log js/console "cancelled")))))
;  
;  (defn app []
;    (let [over-channel (listen (.getElementById js/document "menu") "mouseover")
;          out-channel (listen (.getElementById js/document "menu") "mouseout")]
;      (go-loop []
;        (let [e (<! over-channel)]
;          (open e out-channel))
;        (recur))))
;  (app)

(defn rand-button []
  (rand-nth [:red :yellow :green :blue]))

(def pattern-container (.getElementById js/document "pattern-container"))
(defn restart-game [] (conj [] (rand-button)))

(defn show-pattern [pattern]
  (set! (.-innerText pattern-container) (str pattern)))

(defn correct-so-far [clicks pattern]
  (every?
    (fn [[k v]] (= k v))
    (zipmap clicks pattern)))

(defn start []
  (let [button-channel (listen-query ".controls button" "click")]
    (go-loop [clicks []
              pattern (restart-game)
              pattern-updated? true]

      ; cleaner way to tell if the pattern was updated?
      (if pattern-updated? (show-pattern pattern))

      (let [clicked (<! button-channel)]
        (cond
          (= pattern (conj clicks clicked))
            (recur [] (conj pattern (rand-button)) true)
          (correct-so-far (conj clicks clicked) pattern)
            (recur (conj clicks clicked) pattern false)
          :else (recur [] (restart-game) true))))))

(start)
