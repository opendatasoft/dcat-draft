(ns ods.util
  (:require [incanter.core :refer [to-dataset]]
            [grafter.rdf.protocols :as pr]
            [grafter.rdf.io :as io]
            [clojure.string :as st]))

(defn blank? [v]
  (or (nil? v) (= "" v)))

(defn basic-filter
    "Filters blank triples"
    [triples]
    (filter #(not (blank? (pr/object %1))) triples))

(defn import-rdf
  ([quads-seq destination]
   (import-rdf quads-seq destination basic-filter))
  ([quads-seq destination filter]
   (let [now (java.util.Date.)
         quads (->> quads-seq
                    filter)]
     (pr/add (io/rdf-serializer destination) quads))))

(defn unique-rows
  "Eagerly de-duplicates the dataset. Useful for building smaller files of triples.
If you want to stay lazy then you could let the triplestore de-dupe."
  [dataset]
  (-> dataset
      :rows
      distinct
      to-dataset))
