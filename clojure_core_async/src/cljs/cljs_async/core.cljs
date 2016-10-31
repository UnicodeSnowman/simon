;;; If this namespace requires macros, remember that ClojureScript's
;;; macros are written in Clojure and have to be referenced via the
;;; :require-macros directive where the :as keyword is required, while in Clojure is optional. Even
;;; if you can add files containing macros and compile-time only
;;; functions in the :source-paths setting of the :builds, it is
;;; strongly suggested to add them to the leiningen :source-paths.
(ns cljs-async.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async :refer [put! alts! <! timeout chan]]))

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

(def buttons [:red :yellow :blue :green])
(defn rand-button []
  (rand-nth buttons))

(defn new-game [] (conj [] (rand-button)))

(defn show-pattern
  ([pattern]
   (show-pattern pattern 500))
  ([pattern ms]
    (go
      (doseq [btn pattern]
        (let [elem (.getElementById js/document (name btn))]
         (<! (timeout ms))
         (set! (.-className elem) "button active")
         (<! (timeout ms))
         (set! (.-className elem) "button"))))))

(defn demo []
    (show-pattern (take 12 (cycle buttons)) 50))

(defn correct-so-far [clicks pattern]
  (every?
    (fn [[k v]] (= k v))
    (zipmap clicks pattern)))

(defn start []
  (let [button-channel (listen-query ".controls button" "click")
        initial-pattern (new-game)]
    (show-pattern initial-pattern)
    (go-loop [clicks []
              pattern initial-pattern]
      (let [clicked (<! button-channel)]
        (cond
          (= pattern (conj clicks clicked))
            (let [next-pattern (conj pattern (rand-button))]
              (show-pattern next-pattern)
              (recur [] next-pattern))
          (correct-so-far (conj clicks clicked) pattern)
            (recur (conj clicks clicked) pattern)
          :else (let [initial-pattern (new-game)]
                  (show-pattern initial-pattern)
                  (recur [] initial-pattern)))))))

(go
  (demo)
  (<! (timeout 1500))
  (start))
