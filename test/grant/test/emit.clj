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
