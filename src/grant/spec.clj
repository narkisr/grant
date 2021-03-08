(ns grant.spec
  "Converting spec EDN into a datascript instance we query"
  (:require
   [clojure.math.combinatorics :refer (cartesian-product)]
   [com.rpl.specter :refer (ALL transform cond-path must keypath)]
   [datascript.core :as d]
   [clojure.edn :as edn]))

(def db (d/create-conn))

(defn load-facts [spec]
  (d/transact! (d/create-conn) (into [] spec)))

(defn into-datoms [edn]
  (transform [ALL (must :args)]
             (fn [args]
               (->> args
                    (map (fn [a] (if (= (count (flatten a)) 2) #{a} a)))
                    (apply cartesian-product))) edn))
(defn load-spec [edn]
  (:db-after
   (load-facts (into-datoms edn))))

(defn services [db]
  (d/q '[:find ?e
         :where
         [?e :group :service]] db))

(defn user-commands [user db]
  (d/q '[:find ?bin ?args
         :in $ ?u
         :where
         [?uid :user ?u]
         [?uid :groups ?cmds]
         [?cid :group ?g]
         [?cid :binary ?bin]
         [?cid :args ?args]
         [(?cmds ?g)]] db user))

(defn user-groups [user db]
  (d/q '[:find ?groups .
         :in $ ?u
         :where
         [?uid :user ?u]
         [?uid :groups ?groups]] db user))

(defn missing-group? [db group]
  (empty?
   (d/q '[:find ?cid
          :in $ ?g
          :where
          [?cid :group ?g]] db group)))

(defn all [db ids]
  (d/pull-many db ["*"] (flatten (into [] ids))))

(defn users [db]
  (all db (d/q '[:find ?e :where [?e :user _]] db)))

(defn commands [db]
  (group-by :group (all db (d/q '[:find ?g :where [?g :group _]] db))))

(defn defaults [db]
  (all db (d/q '[:find ?e :where [?e :default _]] db)))

(comment
  (def edn (clojure.edn/read-string (slurp "test/resources/spec.edn")))
  (def spec (load-spec edn))
  (commands spec)
  (defaults spec)
  (user-groups "re-ops" spec)
  (filter (partial missing-group? spec) (user-groups "re-ops" spec))
  (services spec))
