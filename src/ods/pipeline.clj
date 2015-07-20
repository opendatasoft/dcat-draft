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

(def c "datasets.csv")

(def cols [:datasetid :title :description :theme :keyword :license :language :modified :data_processed :metadata_processed :publisher :references :odi_certificate_url :records_count :attributions :source_domain :source_domain_title :source_domain_address :source_dataset :oauth_scope :dcatcreated :dcatissued :dcatcreator :dcatcontributor :dcataccrualperiodicity :dcatspatial :dcattemporal :dcatgranularity :dcatdataquality :inspirefile_identifier :inspirehierarchy_level :inspirehierarchy_level_name :inspirecontact_individual_name :inspirecontact_position :inspirecontact_address :inspirecontact_email :inspireidentification_purpose :inspireextend_description :inspireextend_bounding_box_westbound_longitude :inspireextend_bounding_box_eastbound_longitude :inspireextend_bounding_box_southbound_latitude :inspireextend_bounding_box_northbound_latitude :exploredownload_count])

(def catalog-template
  (graph-fn [{:keys [dataset-uri datasetid title description modified publisher
                     keyword references language license theme-uri theme-label]}]
            (graph (base-graph "catalog")
                   [catalog
                    [rdf:a dcat:Catalog]
                    [dcterms:title (s "Public's catalog")]
                    [dcterms:language (lang language)]
                    [dcterms:issued (s "2015-07-20T14:00:00+00:00")]
                    [dcat:dataset dataset-uri]]

                   [theme-cs
                    [rdf:a skos:ConceptScheme]
                    [skos:prefLabel (s "A Set of data themes")]
                    [skos:topConceptOf theme-uri]]

                   [dataset-uri
                    [rdf:a dcat:Dataset]
                    [dcterms:identifier (s datasetid)]
                    [dcterms:publisher (s publisher)]
                    [dcterms:license (->license license)]
                    [dcterms:title (s title)]
                    [dcterms:modified (s modified)]
                    [dcterms:language (lang language)]
                    [dcat:theme theme-uri]
                    [dcat:keyword ]
                    [dcterms:references (urify-uri references)]
                    [dcterms:description (s description)]]
                   
                   [theme-uri
                    [rdf:a skos:ConceptScheme]
                    [skos:inScheme theme-cs]
                    [skos:prefLabel (s theme-label)]])))

(defpipe convert-catalog
  "Pipeline to convert tabular ODS catalog data"
  [data-file]
  (-> (read-dataset data-file)
      (make-dataset move-first-row-to-header)
      (rename-columns (comp keyword slugify))
      (derive-column :dataset-uri [:datasetid] base-domain)
      (derive-column :theme-label [:theme] ->theme)
      (derive-column :theme-uri [:theme-label] (comp theme-id slugify))
      ;;(columns [:modified])
      ))

(defgraft catalog->graph
  "Pipeline to convert the tabular ODS catalog data sheet into graph data."
  convert-catalog catalog-template)

(defn catalog-pipeline
  [data-file output]
  (-> (convert-catalog data-file)
      catalog-template
      (import-rdf output))
  (println "Grafted: " data-file))
