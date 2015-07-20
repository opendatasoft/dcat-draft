(ns ods.core
  (:require [ods.pipeline :refer [catalog-pipeline]]))

(defn -main
  "run the pipeline"
  [& [path output]]

  (when-not (and path output)
    (println "Usage: lein run <input-file.csv> <output-file.(nt|rdf|n3|ttl)>")
    (System/exit 0))

  
  (println "=> Start pipeline")

  (catalog-pipeline path output)

  (println "=> DONE!"))
