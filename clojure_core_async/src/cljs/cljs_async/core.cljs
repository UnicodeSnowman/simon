;;; If this namespace requires macros, remember that ClojureScript's
;;; macros are written in Clojure and have to be referenced via the
;;; :require-macros directive where the :as keyword is required, while in Clojure is optional. Even
;;; if you can add files containing macros and compile-time only
;;; functions in the :source-paths setting of the :builds, it is
;;; strongly suggested to add them to the leiningen :source-paths.

;(cemerick.piggieback/cljs-repl (weasel.repl.websocket/repl-env :ip "0.0.0.0" :port 9001))

(ns cljs-async.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :as async :refer [put! alts! <! timeout chan]]
            [weasel.repl :as repl]))

  (when-not (repl/alive?)
    (repl/connect "ws://localhost:9001"))

  (defn listen [el evt]
    (let [c (chan)]
      (.addEventListener el evt (fn [e] (put! c e)))
      c))

  (defn listen-query [query evt]
    (let [c (chan)
          els (.from js/Array (.querySelectorAll js/document query))
          handler #(put! c (keyword (-> % .-target .-id)))]
      (doseq [el els]
        (.addEventListener el evt handler))
      [c #(.removeEventListener (first els) evt handler)]))

(def buttons {:red 220
              :yellow 440
              :blue 660
              :green 880})

(defn rand-button []
  (rand-nth (keys buttons)))

(defonce audio-context (js/AudioContext.))
(defonce gain-node
  (let [node (.createGain audio-context)]
    (.connect node (.-destination audio-context))
    node))

(defn play [f]
  (let [osc (.createOscillator audio-context)]
    (.connect osc gain-node)
    (set! (.-value (.-frequency osc)) f)
    (.start osc)
    osc))

(defn stop [osc]
  (.stop osc))

(defn show-pattern
  ([pattern]
   (show-pattern pattern 500))
  ([pattern ms]
    (go
      (doseq [btn pattern]
        (let [elem (.getElementById js/document (name btn))]
         (<! (timeout ms))
         (set! (.-className elem) "button active")
         (let [osc (play (buttons btn))]
           (<! (timeout ms))
           (stop osc))
         (set! (.-className elem) "button"))))))

(defn correct-so-far [clicks pattern]
  (every?
    (fn [[k v]] (= k v))
    (zipmap clicks pattern)))

(defn new-game [] (conj [] (rand-button)))

(defn start []
  (let [[button-down-channel _] (listen-query ".controls button" "mousedown")
        [button-up-channel _] (listen-query ".controls button" "mouseup")
        initial-pattern (new-game)]
    (show-pattern initial-pattern)
    (go-loop [clicks []
              pattern initial-pattern]
      (let [clicked (<! button-down-channel)
            osc (play (buttons clicked))
            [mouseout-channel remove-mouseout-listener] (listen-query ".controls button" "mouseout")]
        ; TODO deregister from mouseout... or, is there a better/idiomatic core.async
        ; way to handle sequencing channel events like this? i.e. only pay attention
        ; to mouseout events *after* we get a mousedown event (but only until we
        ; get a mouseup event or mouseout event)
        ; maybe using mix? https://yobriefca.se/blog/2014/06/01/combining-and-controlling-channels-with-core-dot-asyncs-merge-and-mix/
        ; could also allow listen-query to return tuple of channel and "deregister" fn
        (alts! [button-up-channel mouseout-channel])
        (remove-mouseout-listener)
        (stop osc)
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

(defn demo []
    (show-pattern (take 12 (cycle (keys buttons))) 100))

(go
  (demo)
  (<! (timeout 2500))
  (start))
