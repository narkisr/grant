(ns grant.rules
  (:require
   [clara.rules :refer :all]))

(defn fact-type [fact]
  (:type fact))

(def session
  (atom (mk-session 'grant.rules :fact-type-fn fact-type :cache false)))

(defn update- [facts]
  (let [new-facts (reduce (fn [s fact] (insert s fact)) @session facts)]
    (reset! session (fire-rules new-facts))))

(derive ::alias ::sudoer)
(derive ::spec ::sudoer)
(derive ::commandname ::sudoer)

(defrule wildcards-cmds
  "A rule that warns us about wildcards"
  [?e <- ::wildcard]
  =>
  (println ?e))
