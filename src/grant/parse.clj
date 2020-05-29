(ns grant.parse
  "Parsing sudoer files"
  (:require
   [clojure.walk :as w]
   [com.rpl.specter :refer :all]
   [instaparse.core :as insta]))

(def whitespace
  (insta/parser "whitespace ::= #'\\h+'"))

(def sudoers
  (insta/parser "resources/sudo.ebnf" :input-format :ebnf :auto-whitespace whitespace))

(defn key-pair? [v]
  (and (sequential? v) (= (count v) 2) (keyword? (first v)) (string? (second v))))

(defn key-map? [v]
  (and (sequential? v) (= (count v) 2) (keyword? (first v)) (map? (second v))))

(defn key-list? [v]
  (and (sequential? v) (keyword? (first v)) (sequential? (second v))))

(defn key-maps? [v]
  (and (sequential? v) (keyword? (first v)) (and (sequential? (rest v)) (every? map? (rest v)))))

(defn process [output]
  (w/postwalk
   (fn [v]
     (cond
       (key-pair? v) (apply hash-map v)
       (key-map? v) {(first v) (second v)}
       (key-maps? v) {(first v) (rest v)}
       (key-list? v) {(first v) (rest v)}
       :else v)) output))

(comment
  (sudoers (slurp "test/resources/re-ops")))

