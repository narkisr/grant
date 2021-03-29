(ns grant.core
  (:gen-class)
  (:require
   [clojure.spec.alpha :as spec]
   [grant.spec :refer (load-spec)]
   [grant.generate :refer (create-sudoers)]
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

(defn generate [{:keys [f t]}]
  (spit t (create-sudoers (load-spec f))))

(spec/def ::non-empty-file (fn [f] (and (not (nil? f)) (not (= "" (.getPath f))))))

(spec/def ::spec-edn (fn [s] (not (empty? s))))

(def cli
  {:app {:command     "grant"
         :description "Sudoers file analysis and generation"
         :version     "0.1.0"}

   :global-opts []

   :commands    [{:command     "analyse"
                  :description "Parse and extract facts and run analysis rules on top of them reporting security issues"
                  :opts        [{:option "f" :as "file" :type :slurp :spec ::non-empty-file}
                                {:option "p" :as "pretty" :type :flag}]
                  :runs        analyse}
                 {:command     "parse"
                  :description "Parse the provided file and print its output in ast format"
                  :opts        [{:option "f" :as "file" :type :slurp :spec ::non-empty-file}
                                {:option "p" :as "pretty" :type :flag}]
                  :runs        parse}
                 {:command     "generate"
                  :description "Generate a sudoers file from a specification"
                  :opts        [{:option "f" :as "file" :type :ednfile :spec ::spec-edn}
                                {:option "t" :as "target" :type :string}]
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
