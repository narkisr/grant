(ns grant.extract
  "Extract information from the parsed sudoer files"
  (:require
   [meander.epsilon :as m]
   [meander.strategy.epsilon :as s]))

(defn folder-violations
  "Full folder access enabled in user spec or command alias, this is a violation of Rule 1"
  [m]
  (into #{}
        (m/search m
                  (m/$ [:cmnd-alias ?alias-name (m/scan (m/pred (fn [[[k _] & rs]] (and (empty? rs) (= k :directory))) ?command))])
                  {:type :cmnd-alias :alias-name ?alias-name :command ?command :violation :rule-1}
                  (m/$ [:user-spec ?users ?hosts
                        (m/or
                         (m/$ [[:directory _] ..1  :as ?command])
                         (m/$ [_ ..1 [:directory _]  :as ?command])
                         (m/$ [_ ..1 [:directory _] ..1  _ :as ?command]))])
                  {:type :cmnd-alias :users ?users :command ?command :violation :rule-1})))

(defn wildcard-violations
  "Wildcards used in user spec or cmnd alias, this is a violation of Rule 2"
  [m]
  (into #{}
        (m/search m
                  (m/$ [:cmnd-alias ?alias-name
                        (m/scan (m/scan (m/pred (partial some (fn [k] (= k :wildcard)))) :as ?command))])
                  {:type :cmnd-alias :alias-name ?alias-name :command ?command :violation :rule-2}
                  (m/$ [:user-spec ?users ?hosts
                        (m/or
                         (m/$ [[:wildcard _] ..1  :as ?command])
                         (m/$ [_ ..1 [:wildcard _]  :as ?command])
                         (m/$ [_ ..1 [:wildcard _] ..1  _ :as ?command]))])
                  {:type :user-spec :users ?users :hosts ?hosts :command ?command :violation :rule-2})))

(defn nopasswd-violations
  "NOPASSWD tag is used in user-spec followed by an ALL alias, violation of Rule 3"
  [m]
  (let [nopasswd #{[:tag "NOPASSWD"]} all #{[:alias-name "ALL"]}]
    (into #{}
          (m/search m
                    (m/$
                     [:user-spec ?users ?hosts
                      (m/scan [[:runas (m/pred (fn [v] (some all v)))]
                               [:tags (m/pred (fn [v] (some nopasswd v)))] ?aliases] :as ?command)])
                    {:type :cmnd-alias :users ?users :command ?command :violation :rule-3}))))

(defn negation-violations
  "Negation is used with in user-spec to exclude command, Rule 4"
  [m]
  (into #{}
        (m/search m
                  (m/$ [:user-spec [[:user ?user]] ?host
                        (m/or
                         (m/$ [[:not [:alias-name _]] ..1  :as ?command])
                         (m/$ [_ ..1  [:not [:alias-name _]] :as ?command])
                         (m/$ [_ ..1 [:not [:alias-name _]] ..1  _ :as ?command]))])
                  {:type :cmnd-alias :user ?user :command ?command :violation :rule-4})))

(defn search [ast]
  (let [checks [wildcard-violations folder-violations nopasswd-violations negation-violations]]
    (apply clojure.set/union (map (fn [f] (f ast)) checks))))

(comment
  (negation-violations (grant.parse/process (grant.parse/sudoers (slurp "test/resources/combined"))))
  (nopasswd-violations (grant.parse/process (grant.parse/sudoers "re-ops ALL=(ALL) NOPASSWD: ALL"))))
