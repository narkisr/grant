(ns grant.test.emit
  "Test AST emit to sudoer file format"
  (:require
   [grant.emit :refer [emit]]
   [clojure.test :refer :all]))

(deftest defaults
  (let [run-as-default [:default/runas "root" [[:not [:identifier "set_logname"]]]]
        cmnd-default [:default/cmnd-alias "PAGERS" [[[:identifier "noexec"]]]]
        server-default [:default/servers "SERVERS" [[[:identifier "log_year"]] [[:equals [:identifier "logfile"] [:value [:file "/var/log/sudo.log"]]]]]]
        subtract-default  [:default [[[:not [:identifier "env_reset"]]] [[:subtract [:identifier "env_delete"] [:value [:environment "PATH"]]]]]]]
    (is (= (emit run-as-default) ["Defaults>root" "!set_logname"]))
    (is (= (emit server-default) ["Defaults@SERVERS" "log_year,logfile=/var/log/sudo.log"]))
    (is (= (emit cmnd-default) ["Defaults!PAGERS" "noexec"]))
    (is (= (emit subtract-default) ["Defaults" "!env_reset,env_delete-=PATH"]))))

(deftest user-spec
  (is (= (second
          (emit
           [:sudoers
            [:user-spec
             [[:user "re-ops"] [:user "me"]]
             [[:host [:hostname "ALL"]] [:host [:hostname "bar"]]]
             [[[:runas [[:alias "ALL"]]]
               [:tags [[:tag "NOPASSWD"] [:tag "EXEC"]]]
               [:cmnd-list
                [[:file "/usr/bin/apt"] [:arg "update"]]
                [[:file "/usr/sbin/ufw"] [:arg "status"]]
                [[:directory "/usr/bin/"]]]]]]]))
         ["re-ops,me" "ALL,bar" "=" "(ALL)  NOPASSWD:EXEC: /usr/bin/apt update, /usr/sbin/ufw status, /usr/bin/"]))

  (is (= (second
          (emit
           [:sudoers
            [:user-spec
             [[:user "re-ops"] [:user "me"]]
             [[:host [:hostname "ALL"]] [:host [:hostname "bar"]]]
             [[[:runas [[:alias "ALL"]]]
               [:tags [[:tag "NOPASSWD"] [:tag "EXEC"]]]
               [:cmnd-list
                [[:file "/usr/bin/apt"] [:arg "update"]]]]
              [[:tags [[:tag "EXEC"]]]
               [:cmnd-list
                [[:file "/usr/sbin/ufw"] [:arg "status"]]
                [[:directory "/usr/bin/"]]]]]]]))
         ["re-ops,me" "ALL,bar" "=" "(ALL)  NOPASSWD:EXEC: /usr/bin/apt update , EXEC: /usr/sbin/ufw status, /usr/bin/"]))

  (is (= (second
          (emit
           [:sudoers
            [:user-spec
             [[:user "re-ops"] [:user "me"]]
             [[:host [:hostname "ALL"]] [:host [:hostname "bar"]]]
             [[[:cmnd-list
                [[:file "/usr/sbin/ufw"] [:arg "status"]]
                [[:directory "/usr/bin/"]]]]]]]))
         ["re-ops,me" "ALL,bar" "=" "/usr/sbin/ufw status, /usr/bin/"])))

(deftest cmnd-aliases
  (is (= (second
          (emit
           [:sudoers
            [:cmnd-alias "F"
             [[[:file "/bin/foo"]]
              [[:sha "sha224"] [:digest "9a9800e318b24f26e19ad81ea7ada2762e978c19128603975707d651"] [:file "/foo/bar"]]
              [[:directory "/tmp/bla/"]]]]]))
         ["Cmnd_Alias F = \\ \n " "/bin/foo, \\ \n  sha224:9a9800e318b24f26e19ad81ea7ada2762e978c19128603975707d651 /foo/bar , \\ \n  /tmp/bla/"])))
