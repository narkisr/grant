(ns grant.rules
  (:require
   [meander.epsilon :as m]
   [clara.rules :refer :all]))

(defn fact-type [fact]
  (:type fact))

(derive ::alias ::sudoer)
(derive ::spec ::sudoer)
(derive ::commandname ::sudoer)

(defrule wildcards-violations
  "A rule that warns us about wildcards"
  [?e <- ::user-spec [{:keys [?wildcard]}] (= ?wildcard true)]
  =>
  (println ?e))

(def session
  (atom (mk-session 'grant.rules :fact-type-fn fact-type :cache false)))

(defn update- [facts]
  (let [new-facts (reduce (fn [s fact] (insert s fact)) @session facts)]
    (reset! session (fire-rules new-facts))))

(defn user-spec-wildcards [m]
  (m/search m
            {:sudoers
             (m/scan
              {:user-spec
               {:user-list {:user ?user}
                :cmnd-spec-list (m/scan {:cmnd-spec {:cmnd {:commandname (m/pred (partial some (fn [m] (contains? m :wildcard))) ?command)}}})}})}

            {:type ::user-spec :user ?user :command ?command :wildcard? true}))

(defn add-facts [parse-data]
  (update- (user-spec-wildcards parse-data)))

(comment
  (def single (grant.parse/process (grant.parse/sudoers (slurp "test/resources/single-line-no-passwd"))))
  (add-facts single))
