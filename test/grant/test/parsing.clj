(ns grant.test.parsing
  "Testing parser"
  (:require
   [grant.parse :refer [sudoers process]]
   [grant.extract :refer [cmd-alias-wildcards]]
   [clojure.test :refer :all]))

(deftest cmd-alias
  (is (= (:sudoers (process (sudoers "Cmnd_Alias F = \\ \n /bin/foo , /foo/bar , /bla/bla -a * /tmp/bla/ yeap, /bin/* 'one' ")))
         '{:cmnd-alias {:alias-name "F",
                        :cmnd-list ({:cmnd {:commandname ({:file "/bin/foo"})}}
                                    {:cmnd {:commandname ({:file "/foo/bar"})}}
                                    {:cmnd {:commandname ({:file "/bla/bla"} {:flag "-a"} {:wildcard "*"} {:directory "/tmp/bla/"} {:arg "yeap"})}}
                                    {:cmnd {:commandname ({:directory "/bin/"} {:wildcard "*"} {:arg "'one'"})}})}})))

(deftest cmd-aliases
  (let [{:keys [sudoers] :as data} (process (sudoers (slurp "test/resources/aliases")))
        names #{"C_PIPES" "C_PKG" "C_KERNEL" "C_SERVICE" "C_SYSTEMCTL" "C_USER" "C_SECURITY" "C_DISK" "C_VIRTUAL"}
        aliases (filter #(contains? % :cmnd-alias) sudoers)]
    (is (= (count aliases) 9))
    (is (= (into #{} (map (comp :alias-name :cmnd-alias) aliases)) names))
    (is (= (count (cmd-alias-wildcards data)) 25))))

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

(deftest defaults
  (is (= (:sudoers (process (sudoers (slurp "test/resources/defaults"))))
         '({:default-entry {:parameter-list {:parameter {:value {:user "re-ops"}, :identified "exempt_group"}}}}
           {:default-entry ({:parameter-list ([:parameter "!" {:identified "env_reset"}])} {:parameter-list {:parameter {:value {:environment "PATH"}, :identified "env_delete"}}})}
           {:default-entry {:parameter-list {:parameter {:value {:user "auth"}, :identified "syslog"}}}}
           {:default-entry {:default-type {:runas-list {:runas-member {:user "root"}}}, :parameter-list ([:parameter "!" {:identified "set_logname"}])}}
           {:default-entry {:default-type {:alias-name "FULLTIMERS"}, :parameter-list ([:parameter "!" {:identified "lecture"}])}}
           {:default-entry {:default-type {:user "millert"}, :parameter-list ([:parameter "!" {:identified "authenticate"}])}}
           {:default-entry ({:default-type {:host-list {:host {:hostname "SERVERS"}}}}
                            {:parameter-list {:parameter {:identified "log_year"}}}
                            {:parameter-list {:parameter {:value {:file "/var/log/sudo.log"}, :identified "logfile"}}})}
           {:default-entry {:default-type {:cmnd-list {:cmnd {:alias-name "PAGERS"}}}, :parameter-list {:parameter {:identified "noexec"}}}}))))

(deftest combined
  (is (= (:sudoers (process (sudoers (slurp "test/resources/combined"))))
         '({:default-entry {:parameter-list {:parameter {:value {:user "re-ops"}, :identified "exempt_group"}}}}
           {:default-entry {:default-type {:cmnd-list {:cmnd {:alias-name "PAGERS"}}}, :parameter-list {:parameter {:identified "noexec"}}}}
           {:cmnd-alias {:alias-name "C_PIPES",
                         :cmnd-list ({:cmnd {:commandname ({:file "/usr/bin/tee"} {:wildcard "*"})}} {:cmnd {:commandname ({:file "/usr/bin/tee"} {:flag "-a"} {:wildcard "*"})}})}}
           {:user-spec {:user-list {:user "re-ops"},
                        :host-list {:host {:hostname "ALL"}},
                        :cmnd-spec-list ({:cmnd-spec {:runas-spec {:runas-list {:runas-member {:alias-name "ALL"}}},
                                                      :tag-spec "NOPASSWD:",
                                                      :cmnd {:commandname ({:file "/usr/bin/apt"} {:arg "update"})}}}
                                         {:cmnd-spec {:cmnd {:commandname ({:file "/usr/bin/apt"} {:arg "upgrade"} {:flag "-y"})}}}
                                         {:cmnd-spec {:cmnd {:commandname ({:file "/usr/bin/purge-kernels"})}}}
                                         {:cmnd-spec {:cmnd {:commandname ({:file "/usr/bin/apt-cleanup"})}}}
                                         {:cmnd-spec {:cmnd {:commandname ({:file "/usr/bin/apt-get"} {:arg "install"} {:wildcard "*"} {:flag "-y"})}}}
                                         {:cmnd-spec {:cmnd {:commandname ({:file "/usr/sbin/ufw"} {:arg "status"})}}}
                                         {:cmnd-spec {:cmnd {:commandname ({:file "/usr/bin/nmap"} {:wildcard "*"})}}}
                                         {:cmnd-spec {:cmnd {:commandname ({:file "/usr/bin/netstat"} {:flag "-tnpa"})}}})}}))))
