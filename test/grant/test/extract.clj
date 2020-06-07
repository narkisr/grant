(ns grant.test.extract
  (:require
   [grant.parse :refer [sudoers process]]
   [grant.extract :refer [wildcards-usage folder-usage]]
   [clojure.test :refer :all]))

(deftest wildcard-violations
  (let [cmnd-wildcards (wildcards-usage (process (sudoers (slurp "test/resources/cmnd-aliases"))))
        user-wildcards (wildcards-usage (process (sudoers (slurp "test/resources/single-line-no-passwd"))))]
    (is (= (count cmnd-wildcards) 25))
    (is (= (count  user-wildcards) 2))))

(deftest folder-violations
  (let [cmnd-folder (folder-usage (process (sudoers (slurp "test/resources/cmnd-aliases"))))
        user-folders (folder-usage (process (sudoers (slurp "test/resources/single-line-no-passwd"))))]
    (is (= (count cmnd-folder) 1))
    (is (= (count  user-folders) 1))))
