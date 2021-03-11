(ns user
  (:refer-clojure :exclude  [update list])
  (:require
   [eftest.runner :as eft]
   [clojure.tools.namespace.repl :refer (refresh refresh-all)]
   [clojure.test]))

(defn start-
  "Starts the current development system."
  [])

(defn stop
  "Shuts down and destroys the current development system."
  [])

(defn go
  "Initializes the current development system and starts it running."
  []
  (start-))

(defn reset []
  (stop)
  (refresh :after 'user/go))

(defn run-test
  ([]
   (run-test "test"))
  ([path]
    (eft/run-tests (eft/find-tests path) {:multithread? false})))

(defn history
  ([]
   (history identity))
  ([f]
   (doseq [line (filter f (clojure.string/split (slurp ".lein-repl-history") #"\n"))]
     (println line))))

(defn clrs
  "clean repl"
  []
  (print (str (char 27) "[2J"))
  (print (str (char 27) "[;H")))
