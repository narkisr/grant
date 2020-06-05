(ns grant.parse
  "Parsing sudoer files"
  (:require
   [clojure.core.match :refer [match]]
   [clojure.walk :as w]
   [instaparse.core :as insta]))

(def whitespace
  (insta/parser "whitespace ::= #'\\h+'"))

(defn sudoers [f]
  ((insta/parser "resources/sudo.ebnf" :input-format :ebnf :auto-whitespace whitespace) f))

(defn defaults [d]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:default-entry parameters]]  [:default parameters]
       [[:default-entry [:default-type [:host-list [:host [:hostname hostname]]]] parameters]]  [:default/servers hostname parameters]
       [[:default-entry [:default-type [:runas-list [:runas-member [:user user]]]] parameters]]  [:default/runas user parameters]
       [[:default-entry [:default-type [:alias-name alias-name]] parameters]]  [:default/user-alias alias-name parameters]
       [[:default-entry [:default-type [:user user]] parameters]]  [:default/user user parameters]
       [[:default-entry [:default-type [:cmnd-list [:cmnd [:alias-name alias-name]]]] parameters]]  [:default/cmnd-alias alias-name parameters]
       [[:parameter-list & parameters]]  parameters
       [[:parameter "!" identifier]]  [:not identifier]
       [[:parameter identifier value]]  [identifier value]
       [[:parameter identifier]]  [identifier]
       :else v)) d))

(defn cmnd-alias [a]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:cmnd-alias [:alias-name alias-name] [:cmnd-list & cmnd-list]]] [:cmnd-alias alias-name cmnd-list]
       [[:cmnd [:commandname & commands]]] commands
       :else v)) a))

(defn user-spec [u]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:user-spec [:user-list & users] [:host-list & hosts] [:cmnd-spec-list & commands]]] [:user-spec users hosts commands]
       [[:cmnd-spec spec]] spec
       [[:cmnd-spec & spec]] spec
       [[:runas-spec [:runas-list [:runas-member [:alias-name alias-name]]]]] [:runas [:alias alias-name]]
       [[:tag-spec tag]] [:tag tag]
       [[:cmnd [:alias-name alias-name]]] [:alias-name alias-name]
       [[:cmnd [:commandname & commands]]] commands
       :else v)) u))

(defn user-alias [u]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:user-alias [:name alias-name] [:user-list & users]]] [:user-alias alias-name users]
       [[:user user]] user
       :else v)) u))

(defn runas-alias [u]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:runas-alias [:name alias-name] [:runas-list & users]]] [:runas-alias alias-name users]
       [[:runas-member [:user user]]] user
       :else v)) u))

(defn host-alias [u]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:host-alias [:name alias-name] [:host-list & hosts]]] [:host-alias alias-name hosts]
       [[:host host]] host
       :else v)) u))

(defn process
  "Converting parser result to grant simplified form "
  [output]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:default-entry _]] (defaults v)
       [[:default-entry & _]] (defaults v)
       [[:cmnd-alias & _]] (cmnd-alias v)
       [[:user-alias & _]] (user-alias v)
       [[:runas-alias & _]] (runas-alias v)
       [[:host-alias & _]] (host-alias v)
       [[:user-spec & _]] (user-spec v)
       :else v)) output))

(comment
  (sudoers (slurp "test/resources/single-line-no-passwd")))
