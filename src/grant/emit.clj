(ns grant.emit
  "Generating sudoers file from AST form"
  (:require
   [clojure.core.match :refer [match]]
   [clojure.walk :as w]
   [clojure.core.strint :refer (<<)]))

(defn emit
  "Processing sudoers file ast form and generating a string output"
  [ast]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:default/run-as r & vs]]  [(str "Defaults>" r) (clojure.string/join "" (flatten vs))]
       [[:default & vs]]  ["Defaults" (clojure.string/join "" (flatten vs))]
       [[:not vs]]  ["!" vs]
       [[:identifier i & rs]]  [i rs]
       [[:value i & rs]]  [i rs]
       [[:subtract f s]]  [f "-=" s]
       [[:equals f s]]  [f "=" s ","]
       :else v)) ast))

(comment
  (def cmnd-alias [:cmnd-alias "C_PIPES"
                   [:file "/usr/bin/tee" :wildcard "*"]
                   [:file "/usr/bin/tee" :flag "-a" :wildcard "*"]])

  (def user-spec [:user-spec
                  [:user "re-ops" :hosts "ALL"]
                  [:tag "NOPASSWD" :alias-name "C_SERVICE"]
                  [:alias-name "C_KERNEL"]])

  (def default-simple [:default [:identified "exempt_group" :value "re-ops"]])

  (def subtract-default [:default [[:not [:identifier "env_reset"]] [:subtract [:identifier "env_delete" :value "PATH"]]]])

  (def run-as-default [:default/run-as "root" [:not [:identified "set_logname"]]])

  (emit [:default [:equals [:identifier "foo"] [:value "PATH"]] [:not [:identifier "env_reset"]]])
  (emit [:default/run-as "root" [:equals [:identifier "foo"] [:value "PATH"]] [:not [:identifier "env_reset"]]]))
