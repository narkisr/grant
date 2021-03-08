(ns grant.test.generate
  "Testing spec generation output"
  (:require
   [clojure.edn :as edn]
   [grant.generate :refer [generate]]
   [grant.spec :refer [load-spec]]
   [clojure.test :refer :all]))

(def package-ast
  [:cmnd-alias
   "PACKAGE"
   [[[:sha "sha256"]
     [:digest ""]
     [:file "/usr/bin/apt-get"] [:arg "update"]]
    [[:sha "sha256"]
     [:digest ""]
     [:file "/usr/bin/apt-get"] [:arg "upgrade"]]
    [[:sha "sha256"]
     [:digest ""]
     [:file "/usr/bin/apt-get"]
     [:arg "install"]
     [:arg "pwgen"]
     [:flag "-y"]]
    [[:sha "sha256"]
     [:digest ""]
     [:file "/usr/bin/apt-get"]
     [:arg "install"]
     [:arg "rlwrap"]
     [:flag "-y"]]
    [[:sha "sha256"]
     [:digest ""]
     [:file "/usr/bin/apt-get"]
     [:arg "install"]
     [:arg "curl"]
     [:flag "-y"]]
    [[:sha "sha256"]
     [:digest ""]
     [:file "/usr/bin/dpkg"]
     [:flag "-i"]
     [:file "osquery_3.3.2_1.linux.amd64.deb"]]
    [[:sha "sha256"]
     [:digest ""]
     [:file "/usr/bin/dpkg"]
     [:flag "-i"]
     [:file "bat_0.12.1_amd64.deb"]]]])

(def user-spec-ast
  [:user-spec
   [[:user "re-ops"]]
   [[:host [:hostname "ALL"]]]
   [[[:tags [[:tag "NOPASSWD"]]] [[:alias-name "PACKAGE"] [:alias-name "VIRTUAL"]]]]])

(defn find-in [f in]
  (first
   (filter
    (fn [v] (and (vector? v) (f v))) in)))

(defn clear-digest [[cmnd name commands]]
  [cmnd name
   (mapv
    (fn [cmd]
      (mapv (fn [[k v]] (if (= k :digest) [:digest ""] [k v])) cmd)) commands)])

(deftest basic-spec
  (let [generated (generate (load-spec (edn/read-string (slurp "test/resources/spec.edn"))))]
    (is (= (clear-digest (find-in (fn [v] (= (second v) "PACKAGE")) generated)) package-ast))
    (is (= (find-in (fn [v] (= :user-spec (first v))) generated) user-spec-ast))))
