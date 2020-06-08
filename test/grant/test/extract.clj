(ns grant.test.extract
  (:require
   [grant.parse :refer [sudoers process]]
   [grant.extract :refer [wildcard-violations folder-violations]]
   [clojure.test :refer :all]))

(deftest wildcards
  (let [cmnd-wildcards (wildcard-violations (process (sudoers (slurp "test/resources/cmnd-aliases"))))
        user-wildcards (wildcard-violations (process (sudoers (slurp "test/resources/single-line-no-passwd"))))]
    (is (= (count cmnd-wildcards) 25))
    (is (= (count  user-wildcards) 2))))

(deftest folders
  (let [cmnd-folder (folder-violations (process (sudoers (slurp "test/resources/cmnd-aliases"))))
        user-folders (folder-violations (process (sudoers (slurp "test/resources/single-line-no-passwd"))))]
    (is (= (count cmnd-folder) 1))
    (is (= (count  user-folders) 1))))
