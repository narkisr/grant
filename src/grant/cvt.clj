(ns grant.cvt
  "Using cvtsudoers to get sudoers file as a datastructure"
  (:require
   [clojure.data.json :as json]
   [clojure.string :refer (lower-case)]
   [clojure.java.shell :refer (sh)]))

(defn into-json [f]
  (let [{:keys [exit out] :as m} (sh "/usr/bin/cvtsudoers" f "-f" "json")]
    (if (= exit 0)
      (json/read-str out :key-fn (comp keyword lower-case))
      (throw (ex-info "failed to run cvtsudoers" m)))))

(comment
  (into-json "test/resources/defaults"))
