(ns grant.test.parsing
  "Testing parser"
  (:require
   [grant.parse :refer [sudoers process]]
   [grant.extract :refer [cmd-alias-wildcards]]
   [clojure.test :refer :all]))

(deftest cmd-alias
  (is (= (process (sudoers "Cmnd_Alias F = \\ \n /bin/foo , /foo/bar , /bla/bla -a * /tmp/bla/ yeap, /bin/* 'one' "))
         [:sudoers
          [:cmnd-alias "F"
           [[[:file "/bin/foo"]] [[:file "/foo/bar"]]
            [[:file "/bla/bla"] [:flag "-a"] [:wildcard "*"] [:directory "/tmp/bla/"] [:arg "yeap"]]
            [[:directory "/bin/"] [:wildcard "*"] [:arg "'one'"]]]]])))

(deftest cmd-aliases
  (let [data (process (sudoers (slurp "test/resources/cmnd-aliases")))
        names #{"C_PIPES" "C_PKG" "C_KERNEL" "C_SERVICE" "C_SYSTEMCTL" "C_USER" "C_SECURITY" "C_DISK" "C_VIRTUAL"}
        aliases (filter #(= (first %) :cmnd-alias) (rest data))]
    (is (= (count aliases) 9))
    (is (= (into #{} (map second aliases)) names))
    (is (= (count (cmd-alias-wildcards data)) 25))))

(deftest runas-aliases
  (is (= (process (sudoers (slurp "test/resources/runas-aliases")))
         [:sudoers
          [:runas-alias "ADMINS" ["root"]]
          [:runas-alias "DB" ["mysql" "psql" "redis"]]
          [:runas-alias "WEB" ["apache" "nginx"]]])))

(deftest user-aliases
  (is (= (process (sudoers (slurp "test/resources/user-aliases")))
         [:sudoers
          [:user-alias "ADMINS" ["john" "joe" "bart"]]
          [:user-alias "DEVELOPERS" ["lisa" "homer"]]
          [:user-alias "SUPPORT" ["marge" "moe"]]])))

(deftest host-aliases
  (is (= (process (sudoers (slurp "test/resources/hosts-aliases")))
         [:sudoers
          [:host-alias "PROD" [[:hostname "server-1"] [:hostname "server-2"] [:hostname "server-3"]]]
          [:host-alias "NETWORKS" [[:ip-addr "192.168.2.122"] [:network "192.168.1.1/23"]]]])))

(deftest single-line-no-passwd
  (is (= (process (sudoers (slurp "test/resources/single-line-no-passwd")))
         [:sudoers
          [:user-spec
           [[:user "re-ops"]]
           [[:host [:hostname "ALL"]]]
           [[[:runas [:alias "ALL"]] [:tag "NOPASSWD:"] [[:file "/usr/bin/apt"] [:arg "update"]]]
            [[:file "/usr/bin/apt"] [:arg "upgrade"] [:flag "-y"]]
            [[:file "/usr/bin/purge-kernels"]]
            [[:file "/usr/bin/apt-cleanup"]]
            [[:file "/usr/bin/apt-get"] [:arg "install"] [:wildcard "*"] [:flag "-y"]]
            [[:file "/usr/sbin/ufw"] [:arg "status"]]
            [[:file "/usr/bin/nmap"] [:wildcard "*"]]
            [[:file "/usr/bin/netstat"] [:flag "-tnpa"]]]]])))

(deftest defaults
  (is (= (process (sudoers (slurp "test/resources/defaults")))
         [:sudoers
          [:default [[[:identifier "exempt_group"] [:value [:user "re-ops"]]]]]
          [:default [[:not [:identifier "env_reset"]] [[:identifier "env_delete"] [:value [:environment "PATH"]]]]]
          [:default [[[:identifier "syslog"] [:value [:user "auth"]]]]]
          [:default/runas "root" [[:not [:identifier "set_logname"]]]]
          [:default/user-alias "FULLTIMERS" [[:not [:identifier "lecture"]]]]
          [:default/user "millert" [[:not [:identifier "authenticate"]]]]
          [:default/servers "SERVERS" [[[:identifier "log_year"]] [[:identifier "logfile"] [:value [:file "/var/log/sudo.log"]]]]]
          [:default/cmnd-alias "PAGERS" [[[:identifier "noexec"]]]]])))

(deftest combined
  (is (= (process (sudoers (slurp "test/resources/combined")))
         [:sudoers
          [:default [[[:identifier "exempt_group"] [:value [:user "re-ops"]]]]]
          [:default/cmnd-alias "PAGERS" [[[:identifier "noexec"]]]]
          [:cmnd-alias "C_PIPES" [[[:file "/usr/bin/tee"] [:wildcard "*"]] [[:file "/usr/bin/tee"] [:flag "-a"] [:wildcard "*"]]]]
          [:host-alias "PROD" [[:hostname "server-1"] [:hostname "server-2"] [:hostname "server-3"]]]
          [:user-alias "SUPPORT" ["marge" "moe"]]
          [:runas-alias "WEB" ["apache" "nginx"]]
          [:user-spec
           [[:user "re-ops"]]
           [[:host [:hostname "ALL"]]]
           [[[:runas [:alias "ALL"]] [:tag "NOPASSWD:"] [[:file "/usr/bin/apt"] [:arg "update"]]]
            [[:file "/usr/bin/apt"] [:arg "upgrade"] [:flag "-y"]]
            [[:file "/usr/bin/purge-kernels"]]
            [[:file "/usr/bin/apt-cleanup"]]
            [[:file "/usr/bin/apt-get"] [:arg "install"] [:wildcard "*"] [:flag "-y"]]
            [[:file "/usr/sbin/ufw"] [:arg "status"]]
            [[:file "/usr/bin/nmap"] [:wildcard "*"]]
            [[:file "/usr/bin/netstat"] [:flag "-tnpa"]]]]])))
