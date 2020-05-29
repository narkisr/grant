(ns grant.test.parsing
  "Testing parser"
  (:require
   [grant.parse :refer [sudoers process]]
   [clojure.test :refer :all]))

(deftest cmd-alias
  (is (= (process (sudoers "Cmnd_Alias F = \\ \n /bin/foo , /foo/bar , /bla/bla -a * , /bin/* "))
         '{:sudoers {:cmnd-alias ({:alias-name "F"}
                                  {:cmnd-list ({:cmnd {:commandname {:file "/bin/foo"}}}
                                               {:cmnd {:commandname {:file "/foo/bar"}}}
                                               {:cmnd {:commandname ({:file "/bla/bla"} {:flag "-a"} {:wildcard "*"})}}
                                               {:cmnd {:directory "/bin/*"}})})}})))

(deftest re-ops
  (is (= (first (:sudoers (process (sudoers (slurp "test/resources/re-ops")))))
         '{:cmnd-alias ({:alias-name "C_PIPES"}
                        {:cmnd-list ({:cmnd {:commandname ({:file "/usr/bin/tee"} {:wildcard "*"})}}
                                     {:cmnd {:commandname ({:file "/usr/bin/tee"} {:flag "-a"} {:wildcard "*"})}}
                                     {:cmnd {:commandname ({:file "/usr/bin/sed"} {:flag "-i"} {:wildcard "*"})}}
                                     {:cmnd {:commandname ({:file "/usr/bin/tail"} {:flag "-f"})}}
                                     {:cmnd {:directory "/var/log/*"}})})})))
