(ns grant.spec
  "Spec into datoms"
  (:require
   [clojure.math.combinatorics :refer (cartesian-product)]
   [com.rpl.specter :refer (ALL transform cond-path must keypath)]
   [datascript.core :as d]
   [clojure.edn :as edn]))

(def db (d/create-conn))

(defn load-facts [spec]
  (d/transact! (d/create-conn) (into [] spec)))

(defn load-spec [f]
  (let [spec (edn/read-string (slurp f))]
    (:db-after
     (load-facts
      (transform [ALL (must :args)]
                 (fn [args]
                   (->> args
                        (map (fn [a] (if (= (count (flatten a)) 2) #{a} a)))
                        (apply cartesian-product))) spec)))))

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
  (all db (d/q '[:find ?g :where [?g :group _]] db)))

(comment
  (def spec (load-spec "test/resources/spec.edn"))
  (commands spec)
  (user-groups "re-ops" spec)
  (filter (partial missing-group? spec) (user-groups "re-ops" spec))
  (services spec))
