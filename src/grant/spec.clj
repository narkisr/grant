(ns grant.spec
  "Spec into datoms"
  (:require
   [clojure.math.combinatorics :refer (cartesian-product)]
   [com.rpl.specter :refer (ALL MAP-VALS  multi-path srange filterer transform select traverse keypath)]
   [datascript.core :as d]
   [clojure.edn :as edn]))

(def schema {:command/group {:db/type :db.type/identity}
             :command/groups {:db/cardinality :db.cardinality/many}})

(def db (d/create-conn))

(defn load-spec [f]
  (let [spec (edn/read-string (slurp f))]
    (transform [:sudoers/commands ALL :command/args]
               (fn [args]
                 (->> args
                      (map (fn [a] (if (= (count (flatten a)) 2) #{a} a)))
                      (apply cartesian-product))) spec)))

(defn load-facts [spec]
  (d/transact! db (into [] (:sudoers/commands spec)))
  (d/transact! db (into [] (:sudoers/users spec))))

(defn services []
  (d/q '[:find ?e
         :where
         [?e :command/group :service]] @db))

(defn user-commands [user]
  (d/q '[:find ?bin ?args
         :in $ ?u
         :where
         [?uid :user/name ?u]
         [?uid :command/groups ?cmds]
         [?cid :command/group ?g]
         [?cid :command/binary ?bin]
         [?cid :command/args ?args]
         [(?cmds ?g)]] @db user))

(defn user-groups [user]
  (d/q '[:find ?groups .
         :in $ ?u
         :where
         [?uid :user/name ?u]
         [?uid :command/groups ?groups]] @db user))

(defn missing-group? [group]
  (empty?
   (d/q '[:find ?cid
          :in $ ?g
          :where
          [?cid :command/group ?g]] @db group)))

(comment
  (load-facts (load-spec "test/resources/spec.edn"))
  (user-commands "ronen")
  (user-groups "ronen")
  (filter missing-group? (user-groups "re-ops"))
  (services))


