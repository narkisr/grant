(ns grant.emit
  "Generating sudoers file from AST form"
  (:require
   [clojure.core.match :refer [match]]
   [clojure.walk :as w]
   [clojure.string :refer (join)]))

(defn emit-defaults [ast]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:default/runas user parameters]]  [(str "Defaults>" user) (clojure.string/join "," (flatten parameters))]
       [[:default/servers server parameters]]  [(str "Defaults@" server) (clojure.string/join "," (flatten parameters))]
       [[:default/cmnd-alias alias-name parameters]]  [(str "Defaults!" alias-name) (clojure.string/join "," (flatten parameters))]
       [[:default & vs]]  ["Defaults" (clojure.string/join "," (flatten vs))]
       [[:not i]]  (str "!" i)
       [[:identifier i]]  i
       [[:value [_ i]]] i
       [[:subtract f s]]  (str f "-=" s)
       [[:add f s]]  (str f "+=" s)
       [[:equals f s]]  (str f "=" s)
       :else  v)) ast))

(defn cmd-emit [cmd]
  (match cmd
    [a b cmnd-list] (join " " [a b (join ", " (map (partial join " ") cmnd-list))])
    [a cmnd-list] (join " " [a (join ", " (map (partial join " ") cmnd-list))])
    [cmnd-list] (join ", " (map (partial join " ") cmnd-list))))

(defn emit-user-spec [ast]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:user-spec users hosts cmds]]
       [(join "," (map second users)) (join "," (map second hosts))
        "="
        (join " , " (map cmd-emit cmds))]
       [[:runas & [aliases]]] (str "(" (join "," aliases) ") ")
       [[:tags tags]] (str (join ":" tags) ":")
       [[:tag tag]] tag
       [[:cmnd-list & commands]] commands
       [[:file file]] file
       [[:arg arg]] arg
       [[:host host]] host
       [[:alias alias-name]] alias-name
       [[:alias-name alias-name]] alias-name
       [[:directory directory]] directory
       :else v)) ast))

(defn emit-cmnd-alias [ast]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:user-spec _ _ _]] (emit-user-spec v)
       [[:cmnd-alias name cmds]] [(str "Cmnd_Alias " name " = \\ \n ") (join ", \\ \n  " (flatten cmds))]
       [[[:sha sha] [:digest digest] file & r]] [(str sha ":" digest " " file " " (join " " r))]
       [[:directory directory]] directory
       [[:file file]] file
       [[:arg arg]] arg
       [[:flag flag]] flag
       :else v)) ast))

(defn emit
  "Processing sudoers file ast form and generating a string output"
  [ast]
  (w/postwalk
   (fn [v]
     (match [v]
       [[:default/runas _ _]] (emit-defaults v)
       [[:default/servers _ _]] (emit-defaults v)
       [[:default/cmnd-alias _ _]] (emit-defaults v)
       [[:default & _]] (emit-defaults v)
       [[:user-spec _ _ _]] (emit-user-spec v)
       [[:cmnd-alias _ _]] (emit-cmnd-alias v)
       :else v)) ast))

(comment
  (def cmnd-alias
    [:sudoers
     [:cmnd-alias "F"
      [[[:file "/bin/1"]]
       [[:sha "sha224"] [:digest "9a9800e318b24f26e19ad81ea7ada2762e978c19128603975707d651"] [:file "/bin/2"] [:arg "foo"]]
       [[:directory "/bin/3"]]]]])
  (emit cmnd-alias))

