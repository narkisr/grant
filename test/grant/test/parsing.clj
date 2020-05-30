(ns grant.test.parsing
  "Testing parser"
  (:require
   [grant.parse :refer [sudoers process]]
   [clojure.test :refer :all]))

(deftest cmd-alias
  (is (= (process (sudoers "Cmnd_Alias F = \\ \n /bin/foo , /foo/bar , /bla/bla -a * , /bin/* "))
         '{:sudoers {:cmnd-alias {:alias-name "F"
                                  :cmnd-list ({:cmnd {:commandname ({:file "/bin/foo"})}}
                                              {:cmnd {:commandname ({:file "/foo/bar"})}}
                                              {:cmnd {:commandname ({:file "/bla/bla"} {:flag "-a"} {:wildcard "*"})}}
                                              {:cmnd {:directory "/bin/*"}})}}})))

(deftest cmd-aliases
  (is (= (first (:sudoers (process (sudoers (slurp "test/resources/aliases")))))
         '{:cmnd-alias {:alias-name "C_PIPES"
                        :cmnd-list ({:cmnd {:commandname ({:file "/usr/bin/tee"} {:wildcard "*"})}}
                                    {:cmnd {:commandname ({:file "/usr/bin/tee"} {:flag "-a"} {:wildcard "*"})}}
                                    {:cmnd {:commandname ({:file "/usr/bin/sed"} {:flag "-i"} {:wildcard "*"})}}
                                    {:cmnd {:commandname ({:file "/usr/bin/tail"} {:flag "-f"})}}
                                    {:cmnd {:directory "/var/log/*"}})}})))

(deftest single-line-no-passwd
  (is (= (:sudoers (process (sudoers (slurp "test/resources/single-line-no-passwd"))))
         '{:user-spec {:user-list {:user "re-ops"}
                       :host-list {:host {:hostname "ALL"}}
                       :cmnd-spec-list ({:cmnd-spec {:runas-spec {:runas-list {:runas-member {:alias-name "ALL"}}}
                                                     :tag-spec "NOPASSWD:"
                                                     :cmnd {:commandname ({:file "/usr/bin/apt"} {:arg "update"})}}}
                                        {:cmnd-spec {:cmnd {:commandname ({:file "/usr/bin/apt"} {:arg "upgrade"} {:flag "-y"})}}}
                                        {:cmnd-spec {:cmnd {:commandname ({:file "/usr/bin/purge-kernels"})}}}
                                        {:cmnd-spec {:cmnd {:commandname ({:file "/usr/bin/apt-cleanup"})}}}
                                        {:cmnd-spec {:cmnd {:commandname ({:file "/usr/bin/apt-get"} {:arg "install"} {:wildcard "*"} {:flag "-y"})}}}
                                        {:cmnd-spec {:cmnd {:commandname ({:file "/usr/sbin/ufw"} {:arg "status"})}}}
                                        {:cmnd-spec {:cmnd {:commandname ({:file "/usr/bin/nmap"} {:wildcard "*"})}}}
                                        {:cmnd-spec {:cmnd {:commandname ({:file "/usr/bin/netstat"} {:flag "-tnpa"})}}})}})))
