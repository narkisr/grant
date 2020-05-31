(ns grant.rules
  (:require
   [clara.rules :refer :all]))

(defn fact-type [fact]
  (:type fact))

(derive ::alias ::sudoer)
(derive ::spec ::sudoer)
(derive ::commandname ::sudoer)

(defrule wildcards-violations
  "A rule that warns us about wildcards"
  [?e <- :user-spec [{:keys [wildcard?]}] (= wildcard? true)]
  =>
  (println ?e))

(defn update- [facts]
  (->
   (mk-session 'grant.rules :fact-type-fn fact-type :cache true)
   (insert facts)
   (fire-rules)))

