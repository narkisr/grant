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
       [[:default/runas user parameters]]  [(str "Defaults>" user) (clojure.string/join "," (flatten parameters))]
       [[:default/servers server parameters]]  [(str "Defaults@" server) (clojure.string/join "," (flatten parameters))]
       [[:default/cmnd-alias alias-name parameters]]  [(str "Defaults!" alias-name) (clojure.string/join "," (flatten parameters))]
       [[:default & vs]]  ["Defaults" (clojure.string/join "," (flatten vs))]
       [[:not i]]  (str "!" i)
       [[:identifier i]]  i
       [[:value [_ i]]] i
       [[:subtract f s]]  (str f "-=" s)
       [[:equals f s]]  (str f "=" s)
       :else  v)) ast))

(defn emit-user-spec [ast])

(defn emit
  "Processing sudoers file ast form and generating a string output"
  [ast]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:default/runas _ _]] (emit-defaults v)
       [[:default/servers _ _]] (emit-defaults v)
       [[:default/cmnd-alias _ _]] (emit-defaults v)
       [[:default & _]] (emit-defaults v)
       [[:user-spec _ _ _]] (emit-user-spec v)
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

  (def run-as-default [:default/runas "root" [[[:equals [:identifier "set_logname"] [:value [:user "foo"]]]]]])

  (emit subtract-default)
  (emit run-as-default))
