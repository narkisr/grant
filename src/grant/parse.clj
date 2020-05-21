(ns grant.parse
  "Parsing sudoer files"
  (:require [instaparse.core :as insta]))

(def whitespace
  (insta/parser "whitespace ::= #'\\h+'"))

(def sudoers
  (insta/parser "resources/sudo.ebnf" :input-format :ebnf :auto-whitespace whitespace))

#_(def sudoers
    (insta/parser "Cmnd_List ::= FILE+
                 FILE ::= #'(/[a-zA-Z0-9_-]+)+'" :input-format :ebnf :auto-whitespace :standard))

(comment
  (re-matches #"[A-Z]([A-Z]|[0-9]|\_)*" "O")
  (re-matches #"^/|(/[a-zA-Z0-9_-]+)+/[a-zA-Z0-9_-]+$" "/bin/bla")
  (re-matches #"^/|(/[a-zA-Z0-9_-]+)+/[a-zA-Z0-9_-]+$" "/usr/bin/ls")
  (clojure.pprint/pprint (sudoers (slurp "test/resources/re-ops")))
  (clojure.pprint/pprint (sudoers "/usr/bin /bla/bla "))
  (clojure.pprint/pprint (sudoers "Cmnd_Alias F = \\ \n"))
  (clojure.pprint/pprint (sudoers "Cmnd_Alias F = \\ \n /bin/foo")))

