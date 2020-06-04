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

            {:type :user-spec :user ?user :command ?command :wildcard? true}

            {:sudoers
             (m/scan
              {:user-spec
               {:user-list {:user ?user}
                :cmnd-spec-list (m/scan {:cmnd-spec {:cmnd {:commandname (m/pred (partial some (fn [m] (contains? m :wildcard))) ?command)}}})}})}

            {:type :user-spec :user ?user :command ?command :wildcard? true}))

(defn cmd-alias-wildcards
  "Get wildcards in cmd aliases"
  [m]
  (into #{}
        (m/search m
                  (m/$ [:cmnd-alias ?alias-name (m/scan (m/scan (m/pred (partial some (fn [k] (= k :wildcard)))) :as ?command))])
                  {:type :cmnd-alias :alias-name ?alias-name :command ?command})))

(defn search [m]
  (user-spec-wildcards m))

(comment
  (cmd-alias-wildcards (grant.parse/process (grant.parse/sudoers (slurp "test/resources/aliases"))))
  (user-spec-wildcards (grant.parse/process (grant.parse/sudoers (slurp "test/resources/single-line-no-passwd")))))
