(ns grant.extract
  "Extract information from the parsed sudoer files"
  (:require
   [meander.epsilon :as m]))

(defn user-spec-wildcards
  "Get wildcards in user specs"
  [m]
  (m/search m
            {:sudoers
             {:user-spec
              {:user-list {:user ?user}
               :cmnd-spec-list (m/scan {:cmnd-spec {:cmnd {:commandname (m/pred (partial some (fn [m] (contains? m :wildcard))) ?command)}}})}}}

            {:type ::user-spec :user ?user :command ?command :wildcard? true}

            {:sudoers
             (m/scan
              {:user-spec
               {:user-list {:user ?user}
                :cmnd-spec-list (m/scan {:cmnd-spec {:cmnd {:commandname (m/pred (partial some (fn [m] (contains? m :wildcard))) ?command)}}})}})}

            {:type ::user-spec :user ?user :command ?command :wildcard? true}))

(defn search [m]
  (user-spec-wildcards m))

(comment
  (user-spec-wildcards (grant.parse/process (grant.parse/sudoers (slurp "test/resources/single-line-no-passwd")))))
