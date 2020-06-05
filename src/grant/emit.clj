(ns grant.emit
  "Generating sudoers file from AST form"
  (:require
   [clojure.core.match :refer [match]]
   [clojure.walk :as w]
   [clojure.core.strint :refer (<<)]))

(defn emit-defaults [ast]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:default/runas r vs]]  [(str "Defaults>" r) (clojure.string/join "," (flatten vs))]
       [[:default & vs]]  ["Defaults" (clojure.string/join "," (flatten vs))]
       [[:not i]]  (str "!" i)
       [[:identifier i]]  i
       [[:value [_ i]]] i
       [[:subtract [i ve]]]  (str i "-=" ve)
       [[:equals f s]]  [f "=" s ","]
       :else  v)) ast))

(defn emit
  "Processing sudoers file ast form and generating a string output"
  [ast]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:default/runas _ _]] (emit-defaults v)
       [[:default & _]] (emit-defaults v)
       :else v)) ast))

(comment
  (def cmnd-alias [:cmnd-alias "C_PIPES"
                   [[:file "/usr/bin/tee"] [:wildcard "*"]]
                   [[:file "/usr/bin/tee"] [:flag "-a"] [:wildcard "*"]]])

  (def user-spec [:user-spec
                  [[:user "re-ops"]]
                  [[:hosts "ALL"]]
                  [[[:tag "NOPASSWD"] [:alias-name "C_SERVICE"]] [[:alias-name "C_KERNEL"]]]])

  (def default-simple [:default [[[:identifier "exempt_group"] [:value [:user "re-ops"]]]]])

  (def subtract-default)

  (def run-as-default [:default/runas "root" [[:not [:identifier "set_logname"]]]])

  (emit subtract-default)
  (emit run-as-default))
