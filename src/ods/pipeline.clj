(ns ods.pipeline
    (:require
     [grafter.tabular :refer [defpipe defgraft column-names columns rows
                              derive-column mapc swap drop-rows take-rows
                              read-dataset read-datasets make-dataset
                              move-first-row-to-header _ graph-fn melt
                              test-dataset rename-columns]]
     [grafter.rdf.protocols :refer [->Quad]]
     [grafter.rdf.templater :refer [graph]]
     [grafter.vocabularies.rdf :refer :all]
     [grafter.vocabularies.dcterms :refer :all]
     [grafter.vocabularies.foaf :refer :all]
     [grafter.vocabularies.dcat :refer [dcat:Dataset dcat:theme]]
     [grafter.vocabularies.skos :refer :all]
     [ods.prefix :refer :all]
     [ods.util :refer [import-rdf unique-rows]]
     [ods.transform :refer :all]))

(def catalog-template
  (graph-fn [{:keys [dataset-uri datasetid title description modified publisher
                     keyword references language license theme-uri theme-label
                     theme dl-uri dl-json]}]
            (graph (base-graph "catalog")
                   [catalog
                    [rdf:a dcat:Catalog]
                    [dcterms:title (s "Public's catalog")]
                    [dcterms:description (s "OpenDataSoft Public Catalog")]
                    [dcterms:publisher OpenDataSoft]
                    [foaf:homepage "http://public.opendatasoft.com"]
                    [dcat:themeTaxonomy theme-cs]
                    [dcterms:issued (s "2015-07-20T14:00:00+00:00")]
                    [dcat:dataset dataset-uri]]

                   [OpenDataSoft
                    [rdf:a foaf:Agent]
                    [foaf:name (s "OpenDataSoft")]]

                   [theme-cs
                    [rdf:a skos:ConceptScheme]
                    [dcterms:title (s "Themes")]
                    [skos:prefLabel (s "A Set of data themes")]
                    [skos:topConceptOf theme-uri]]

                   [dataset-uri
                    [rdf:a dcat:Dataset]
                    [dcterms:identifier (s datasetid)]
                    [dcterms:publisher (publisher-uris publisher)]
                    [dcterms:license (->license license)]
                    [dcterms:title (s title)]
                    [dcterms:modified (s modified)]
                    [dcterms:language (lang language)]
                    [dcat:theme theme-uri]
                    [dcat:keyword (s keyword)]
                    [dcterms:references (urify-uri references)]
                    [dcterms:description (if (seq description) (s description) (s "unknown"))]
                    [dcat:distribution dl-json]]

                   [dl-json
                    [rdf:a dcat:Distribution]
                    [dcterms:description (s (str "A json feed of" dataset-uri))]
                    [dcat:accessURL dl-uri]
                    [dcterms:mediaType (s "application/json")]]
                   
                   [theme-uri
                    [rdf:a skos:ConceptScheme]
                    [dcterms:title (s (->theme theme))]
                    [skos:inScheme theme-cs]
                    [skos:prefLabel (s theme-label)]])))

(defpipe convert-catalog
  "Pipeline to convert tabular ODS catalog data"
  [data-file]
  (-> (read-dataset data-file)
      (make-dataset move-first-row-to-header)
      (rename-columns (comp keyword slugify))
      (columns [:datasetid :title :description :theme :keyword :license :language :modified :publisher :references])
      (derive-column :dataset-uri [:datasetid] base-domain)
      (derive-column :dl-uri [:datasetid] ->dl)
      (derive-column :dl-json [:datasetid] ->dl-json)
      (derive-column :theme-label [:theme] ->theme)
      (derive-column :theme-uri [:theme-label] (comp theme-id slugify))))

(defgraft catalog->graph
  "Pipeline to convert the tabular ODS catalog data sheet into graph data."
  convert-catalog catalog-template)

(defn catalog-pipeline
  [data-file output]
  (-> (convert-catalog data-file)
      catalog-template
      (import-rdf output))
  (println "Grafted: " data-file))
