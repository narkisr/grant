(ns grant.parsing
  "Testing parser"
  (:require
   [grant.parse :refer [sudoers]]
   [clojure.test :refer :all]))

(deftest cmd-alias
  (sudoers "Cmnd_Alias F = \\ \n /bin/foo , /foo/bar , /bla/bla -a * , /bin/*\\ ")
  (sudoers (slurp "test/resources/re-ops")))
