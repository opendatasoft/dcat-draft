(ns ods.prefix
  (:require [grafter.rdf :refer [prefixer]]
            [grafter.vocabularies.dcterms :refer [dcterms]]
            [grafter.vocabularies.dcat :refer [dcat]]
            [grafter.vocabularies.foaf :refer [foaf]]))

(def base-domain (prefixer "http://public.opendatasoft.com/api/datasets/1.0/"))
(def base-graph (prefixer "http://public.opendatasoft.com/graph/"))
(def theme-id (prefixer "http://public.opendatasoft.com/id/theme/"))
(def theme-cs "http://public.opendatasoft.com/concept-scheme/themes")
(def OpenDataSoft "http://www.opendatasoft.com")

(def catalog "http://public.opendatasoft.com/api/datasets/1.0/search?rows=-1&format=rdf")
(defn ->dl [s] (str "http://public.opendatasoft.com/explore/dataset/" s "/download/?format=json"))
(defn ->dl-json [s] (str "http://public.opendatasoft.com/explore/dataset/" s "-json"))

(def dcterms:identifier (dcterms "identifier"))
(def dcterms:language (dcterms "language"))
(def dcterms:mediaType (dcterms "mediaType"))

(def dcat:keyword (dcat "keyword"))
(def dcat:Catalog (dcat "Catalog"))
(def dcat:dataset (dcat "dataset"))
(def dcat:themeTaxonomy (dcat "themeTaxonomy"))
(def dcat:downloadURL (dcat "downloadURL"))
(def dcat:distribution (dcat "distribution"))
(def dcat:Distribution (dcat "Distribution"))

(def foaf:Agent (foaf "Agent"))

(def lang (prefixer "http://id.loc.gov/vocabulary/iso639-1/"))

