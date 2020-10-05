(ns grant.generate
  "Generate a secure default version of a sudoers file based on the provided spec"
  (:require
   [digest :refer [sha-256]]
   [clojure.math.combinatorics :as combo]
   [grant.emit :refer [emit]]
   [grant.spec :refer [load-spec]]
   [clojure.java.io :refer [as-file]]))

(defn arg-ast [[k v]]
  [(keyword (name k)) v])

(defn args-ast [prefix args]
  (mapv (fn [branch] (into prefix branch)) args))

(defn group-name [k]
  (clojure.string/upper-case (name k)))

(defn alias-ast
  "convert a groups spec into an alias ast form"
  [groups]
  (mapv (fn [a] [:alias-name (group-name a)]) groups))

(defn user-spec-ast
  "convert user to AST form"
  [{:keys [:user/name :command/groups :tags]}]
  [:user-spec
   [[:user name]]
   [[:host [:hostname "ALL"]]]
   [[[:tags (mapv (fn [t] [:tag t]))] (alias-ast groups)]]])

(defn command-alias-ast
  "Create aliases AST's"
  [{:keys [:command/group :command/binary :command/args]}]
  [:cmnd-alias (group-name group)
   (args-ast [[:sha "sha256"] [:digest (sha-256 (as-file binary))] [:file binary]] args)])

(defn generate-spec [{:keys [:sudoers/commands :sudoers/users]}]
  (into [:sudoers]
        (concat
         (map command-alias-ast commands)
         (map user-spec-ast users))))

(defn create-sudoers [spec]
  (clojure.string/join "\n\n" (map clojure.string/join (rest (emit (generate-spec spec))))))

(comment
  (def spec (load-spec "test/resources/spec.edn"))
  (println (clojure.string/join "\n\n" (map (partial clojure.string/join "") (rest (emit (generate-spec spec)))))))

