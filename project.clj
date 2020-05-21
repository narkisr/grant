(defproject grant "0.0.1"
  :description "Secure sudoers managment utility and library"
  :url "https://github.com/narkisr/grant"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}

  :dependencies [
     [org.clojure/clojure "1.10.1"]

     ; utils
     [me.raynes/fs "1.4.6"]
     [org.clojure/core.incubator "0.1.4"]

     ; parsing
     [instaparse "1.4.10"]
  ]

  :profiles {
    :dev {
      :dependencies [[org.clojure/tools.namespace "0.3.1"]]       
    }            
  }

  :plugins  [[lein-cljfmt "0.5.6"]
             [lein-ancient "0.6.15" :exclusions [org.clojure/clojure]]
             [lein-tag "0.1.0"]
             [lein-set-version "0.3.0"]]


  :aliases {
      "travis" [
        "with-profile" "test" "do" "unit," "integration," "cljfmt" "check"
      ]
   }


  :repositories  {"bintray"  "https://dl.bintray.com/content/narkisr/narkisr-jars"
                  "sonatype" "https://oss.sonatype.org/content/repositories/releases"
                  "libvirt-org" "https://libvirt.org/maven2"}

  :resource-paths  ["src/main/resources/"]

  :source-paths  ["src" "dev"]

  :target-path "target/"

  :test-paths  []

  :repl-options {
    :init-ns user
    :prompt (fn [ns]
              (let [hostname (.getHostName (java.net.InetAddress/getLocalHost))]
                (str "\u001B[35m[\u001B[34m" "grant" "\u001B[31m" "@" "\u001B[36m" hostname "\u001B[35m]\u001B[33mλ:\u001B[m ")))
    :welcome (println "Welcome to grant!" )
    :timeout 120000
  }

)
