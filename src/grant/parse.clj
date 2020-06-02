(ns grant.parse
  "Parsing sudoer files"
  (:require
   [clojure.walk :as w]
   [instaparse.core :as insta]))

(def whitespace
  (insta/parser "whitespace ::= #'\\h+'"))

(defn sudoers [f]
  ((insta/parser "resources/sudo.ebnf" :input-format :ebnf :auto-whitespace whitespace) f))


; Converting parser result to nested maps and seqs


(defn key-pair? [v]
  (and (sequential? v) (= (count v) 2) (keyword? (first v)) (string? (second v))))

(defn key-map? [v]
  (and (sequential? v) (= (count v) 2) (keyword? (first v)) (map? (second v))))

(defn key-list? [v]
  (and (sequential? v) (keyword? (first v)) (sequential? (second v))))

(defn key-maps? [v]
  (and (sequential? v) (keyword? (first v)) (sequential? (rest v)) (every? map? (rest v))))

(defn unique-map-keys? [ms]
  (letfn  [(count-keys [acc m] (-> acc (update :sum + (count (keys m))) (update :uniq (fn [v] (into v (set (keys m)))))))]
    (let [{:keys [sum uniq]} (reduce count-keys {:sum 0 :uniq #{}} ms)]
      (= sum (count uniq)))))

(defn key-mergable-maps?
  "Merging k to maps sequence only if the keys in the map are unique (the merge will not override the keys)"
  [v]
  (and (sequential? v) (keyword? (first v)) (sequential? (rest v)) (every? map? (rest v)) (unique-map-keys? (rest v))))

(defn not-commandname [v]
  (not (= (first v) :commandname)))

(defn process [output]
  (w/postwalk
   (fn [v]
     (cond
       (key-pair? v) (apply hash-map v)
       (and (key-map? v) (not-commandname v)) {(first v) (second v)}
       (and (key-mergable-maps? v) (not-commandname v)) {(first v) (apply merge (rest v))}
       (key-maps? v) {(first v) (rest v)}
       (key-list? v) {(first v) (rest v)}
       :else v)) output))

(comment
  (apply disj #{1 2} #{2})
  (key-mergable-maps?)
  (sudoers (slurp "test/resources/defaults"))
  (sudoers (slurp "test/resources/aliases")))
