(ns grant.parse
  "Parsing sudoer files"
  (:require [instaparse.core :as insta]))

(def whitespace
  (insta/parser "whitespace ::= #'\\h+'"))

(def sudoers
  (insta/parser "resources/sudo.ebnf" :input-format :ebnf :auto-whitespace whitespace))

(comment)

