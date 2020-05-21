(ns grant.emit
  "Generating sudoer files"
  (:require
   [clojure.core.strint :refer (<<)]))

(defn cmd-alias [name]
  (<< "Cmnd_Alias ~{name} = \\\n"))

(defn cmd [command & args]
  (<< "    ~{command}"))

(comment
  {:cmd-alias {:name "C_SERVICE"
               :commands [[:command "/usr/bin/usermod" :flag "-G" :arg "ronen" :flag "-a" :arg "foo"]]}})
