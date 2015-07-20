(defproject ods "0.1.0-SNAPSHOT"
  :description "Grafter project to RDFize ODS Data Catalog"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [grafter "0.5.0"]
                 [grafter/vocabularies "0.1.0"]
                 [org.slf4j/slf4j-jdk14 "1.7.5"]]

  :repl-options {:init (set! *print-length* 200)
                 :init-ns ods.pipeline }

  :jvm-opts ^:replace ["-server"
                       ;;"-XX:+AggressiveOpts"
                       ;;"-XX:+UseFastAccessorMethods"
                       ;;"-XX:+UseCompressedOops"
                       ;;"-Xmx4g"
                       ]

  :main ods.core

  :plugins [[lein-grafter "0.5.0"]])
