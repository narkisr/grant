(ns grant.generate
  "Generate a secure default version of a sudoers file based on the provided spec"
  (:require
   [digest :refer [sha-256]]
   [grant.emit :refer [emit]]
   [grant.spec :refer [users commands defaults]]
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
  (mapv (fn [a] [[:alias-name (group-name a)]]) groups))

(defn default-spec-ast
  "Convert default entry to AST form"
  [{:keys [default]}]
  [:default
   (mapv
    (fn [[k v]]
      (cond
        (= v true) [:identifier k]
        (= v false) [:not [:identifier k]]
        :else [:equals [:identifier k] [:value [:string v]]])) default)])

(defn user-spec-ast
  "Convert user to AST form"
  [{:keys [user groups tags]}]
  [:user-spec
   [[:user user]]
   [[:host [:hostname "ALL"]]]
   [[[:tags (mapv (fn [t] [:tag t]) tags)] (into [:cmnd-list] (alias-ast groups))]]])

(defn command-alias-ast
  "Create aliases AST's"
  [[group commands]]
  [:cmnd-alias (group-name group)
   (reduce
    (fn [acc {:keys [binary args]}]
      (if-not args
        (conj acc [[:sha "sha256"] [:digest (sha-256 (as-file binary))] [:file binary]])
        (apply conj acc (args-ast [[:sha "sha256"] [:digest (sha-256 (as-file binary))] [:file binary]] args)))) [] commands)])

(defn generate [spec]
  (into [:sudoers]
        (concat
         (map default-spec-ast (defaults spec))
         (map command-alias-ast (commands spec))
         (map user-spec-ast (users spec)))))

(defn string-form [output]
  (clojure.string/join "\n\n" (map (partial clojure.string/join " ") output)))

(defn create-sudoers [spec]
  (string-form (rest (emit (generate spec)))))

(comment
  (require '[grant.spec :refer [load-spec]])
  (def spec (load-spec (clojure.edn/read-string (slurp "test/resources/spec.edn"))))
  (spit "/tmp/sudoer"  (create-sudoers spec))
  (println (string-form (rest (emit (generate spec))))))

