(ns grant.core
  (:gen-class)
  (:require
   [grant.generate :refer (spit-sudoers)]
   [grant.extract :refer (search)]
   [grant.parse :refer (sudoers process)]
   [cli-matic.core :refer (run-cmd)]))

(defn analyse [{:keys [f p]}]
  (let [detected (search (process (sudoers f)))]
    (if p
      (clojure.pprint/pprint detected)
      (println detected))))

(defn parse [{:keys [f p]}]
  (let [data (process (sudoers f))]
    (if p
      (clojure.pprint/pprint data)
      (println data))))

(defn generate [{:keys [f]}]
  (spit "/tmp/1" (spit-sudoers f)))

(def cli
  {:app {:command     "grant"
         :description "Sudoers file analysis and generation"
         :version     "0.1.0"}

   :global-opts []

   :commands    [{:command     "analyse"
                  :description "Parse and extract facts and run analysis rules on top of them reporting security issues"
                  :opts        [{:option "f" :as "file" :type :slurp} {:option "p" :as "pretty" :type :flag}]
                  :runs        analyse}
                 {:command     "parse"
                  :description "Parse the provided file and print its output in an edn format"
                  :opts        [{:option "f" :as "file" :type :slurp} {:option "p" :as "pretty" :type :flag}]
                  :runs        parse}
                 {:command     "generate"
                  :description "Generate a sudoers file from a provide specification"
                  :opts        [{:option "f" :as "file" :type :ednfile}]
                  :runs        generate}]})

(defn -main [& args]
  (try
    (run-cmd args cli)
    (System/exit 0)
    (catch Exception e
      (println e)
      (System/exit 1))))

(comment
  (System/getProperty "java.vm.name"))
