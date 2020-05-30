(ns grant.core
  (:gen-class)
  (:require
   [grant.rules :refer [add-facts initialize]]
   [grant.parse :refer (sudoers process)]
   [cli-matic.core :refer (run-cmd)]))

(defn validate [{:keys [f]}]
  (initialize)
  (add-facts (process (sudoers f))))

(defn parse [{:keys [f p]}]
  (let [data (process (sudoers f))]
    (if p
      (clojure.pprint/pprint data)
      (println data))))

(def cli
  {:app {:command     "grant"
         :description "Sudoers file analysis and generation"
         :version     "0.1.0"}

   :global-opts []

   :commands    [{:command     "analyse"
                  :description "Parse and analyse the provided sudoers file reporting possible security issues"
                  :opts        [{:option "f" :as "file" :type :slurp} {:option "p" :as "pretty" :type :flag}]
                  :runs        validate}
                 {:command     "parse"
                  :description "Parse the provided file and print its output in an edn format"
                  :opts        [{:option "f" :as "file" :type :slurp} {:option "p" :as "pretty" :type :flag}]
                  :runs        parse}]})

(defn -main [& args]
  (try
    (run-cmd args cli)
    (System/exit 0)
    (catch Exception e
      (println e)
      (System/exit 1))))
