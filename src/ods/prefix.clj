(ns ods.prefix
  (:require [grafter.rdf :refer [prefixer]]
            [grafter.vocabularies.dcterms :refer [dcterms]]
            [grafter.vocabularies.dcat :refer [dcat]]))

(def base-domain (prefixer "http://public.opendatasoft.com/api/datasets/1.0/"))
(def base-graph (prefixer "http://public.opendatasoft.com/graph/"))

(def catalog "http://public.opendatasoft.com/api/datasets/1.0/search?rows=-1&format=rdf")

(def dcterms:identifier (dcterms "identifier"))
(def dcterms:language (dcterms "language"))
(def dcat:keyword (dcat "keyword"))
