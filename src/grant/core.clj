(ns grant.core
  (:gen-class)
  (:require
   [grant.extract]
   [grant.parse :refer (sudoers process)]
   [grant.rules :refer (update-)]
   [cli-matic.core :refer (run-cmd)]))

(defn analyse [{:keys [f]}]
  (if (.contains (System/getProperty "java.vm.name") "OpenJDK")
    #_(let [facts (search (process (sudoers f)))]
        (update- facts))
    (do
      (println "this command is only available on the OpenJDK binary, native binary doesn't support it at the moment")
      (System/exit 1))))

(defn extract [{:keys [f p]}])

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
                  :description "Parse and extract facts and run analysis rules on top of them reporting security issues"
                  :opts        [{:option "f" :as "file" :type :slurp} {:option "p" :as "pretty" :type :flag}]
                  :runs        analyse}
                 {:command     "extract"
                  :description "Parse sudoers file and extract facts and list them (no analysis)"
                  :opts        [{:option "f" :as "file" :type :slurp} {:option "p" :as "pretty" :type :flag}]
                  :runs        extract}
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

(comment
  (System/getProperty "java.vm.name"))
