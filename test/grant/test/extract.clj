(ns grant.test.extract
  (:require
   [grant.parse :refer [sudoers process]]
   [grant.extract :refer [wildcard-violations folder-violations negation-violations nopasswd-violations]]
   [clojure.test :refer :all]))

(deftest wildcards
  (let [cmnd-wildcards (wildcard-violations (process (sudoers (slurp "test/resources/cmnd-aliases"))))
        user-wildcards (wildcard-violations (process (sudoers (slurp "test/resources/single-line-no-passwd"))))]
    (is (= 25 (count cmnd-wildcards)))
    (is (= 2 (count  user-wildcards)))))

(deftest folders
  (let [cmnd-folder (folder-violations (process (sudoers (slurp "test/resources/cmnd-aliases"))))
        user-folders (folder-violations (process (sudoers (slurp "test/resources/single-line-no-passwd"))))]
    (is (= 1 (count cmnd-folder)))
    (is (= 1 (count user-folders)))))

(deftest negation
  (let [negtaions (negation-violations (process (sudoers (slurp "test/resources/combined"))))]
    (is (= 1 (count negtaions)))))

(deftest nopasswd
  (let [nopasswd-all (nopasswd-violations (process (sudoers "re-ops ALL=(ALL) NOPASSWD: ALL")))]
    (is (= 1 (count nopasswd-all)))))
