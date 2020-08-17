(ns grant.generate
  "Generate a secure default version of a sudoers file based on the provided spec"
  (:require
   [digest :refer [sha-256]]
   [grant.emit :refer [emit]]
   [clojure.java.io :refer [as-file]]
   [clojure.core.strint :refer (<<)]))

(defn group-name [k]
  (clojure.string/upper-case (name k)))

(defn alias-ast
  "convert a groups spec into an alias ast form"
  [groups]
  (mapv (fn [a] [:alias-name (group-name a)]) groups))

(defn user-spec-ast
  "convert user to AST form"
  [{:keys [:user/name :command/groups]}]
  [:user-spec
   [[:user name]]
   [[:host [:hostname "ALL"]]]
   [[[:tags [[:tag "NOPASSWD"]]] (alias-ast groups)]]])

(defn command-alias-ast
  "Create aliases AST's"
  [{:keys [:command/group :command/binary :command/args]}]
  [:cmnd-alias (group-name group)
   [[[:sha "sha256"] [:digest (sha-256 (as-file binary))] [:file binary]]]])

(defn generate-spec [{:keys [:sudoers/commands :sudoers/users]}]
  (into [:sudoers]
        (concat
         (map command-alias-ast commands)
         (map user-spec-ast users))))

(comment
  (require '[clojure.edn :as edn])
  (def spec (edn/read-string (slurp "resources/spec.edn")))
  (emit (generate-spec spec)))
