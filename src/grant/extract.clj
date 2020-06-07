(ns grant.extract
  "Extract information from the parsed sudoer files"
  (:require
   [meander.epsilon :as m]))

(defn folder-usage
  "Full folder access enabled in user spec or command alias, this is a violation of Rule 1"
  [m]
  (into #{}
        (m/search m
                  (m/$ [:cmnd-alias ?alias-name (m/scan (m/pred (fn [[[k _] & rs]] (and (empty? rs) (= k :directory))) ?command))])
                  {:type :cmnd-alias :alias-name ?alias-name :command ?command :violation :rule-1}
                  (m/$ [:user-spec [[:user ?user]] ?host  (m/scan (m/pred (fn [[[k _] & rs]] (and (empty? rs) (= k :directory))) ?command))])
                  {:type :cmnd-alias :user ?user :command ?command :violation :rule-1})))

(defn wildcards-usage
  "Wildcards used in user spec or cmnd alias, this is a violation of Rule 2"
  [m]
  (into #{} (m/search m
                      (m/$ [:cmnd-alias ?alias-name (m/scan (m/scan (m/pred (partial some (fn [k] (= k :wildcard)))) :as ?command))])
                      {:type :cmnd-alias :alias-name ?alias-name :command ?command :violation :rule-2}
                      (m/$ [:user-spec [[:user ?user]] ?host (m/scan (m/scan (m/pred (partial some (fn [k] (= k :wildcard)))) :as ?command))])
                      {:type :user-spec :user ?user :host ?host :command ?command :violation :rule-2})))

(comment
  (wildcards-usage (grant.parse/process (grant.parse/sudoers (slurp "test/resources/cmnd-aliases"))))
  (folder-usage (grant.parse/process (grant.parse/sudoers (slurp "test/resources/cmnd-aliases"))))
  (folder-usage (grant.parse/process (grant.parse/sudoers (slurp "test/resources/single-line-no-passwd")))))
