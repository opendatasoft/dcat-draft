# ODS Data Catalog Graft

A Grafter transformation to RDFize a CSV catalog => DCAT-AP

## Requirement

  * [Leiningen (recommended)](http://leiningen.org/)
  * [rdfcat](http://www.inf.ed.ac.uk/teaching/courses/masws/Coding/build/html/jena.html)

## Usage *with leiningen*

To transform a catalog CSV with the same structure than `catalog.csv`:

    $ lein run catalog.csv output.ttl

    => Start pipeline
    Grafted:  catalog.csv
    => DONE!

    $ rdfcat -out n3 output.ttl prefixes.ttl > catalog.ttl

    $ open catalog.ttl

## Usage *without leiningen*

    $ java -jar ods-0.1.0-SNAPSHOT-standalone.jar catalog.csv output.ttl

    => Start pipeline
    Grafted:  catalog.csv
    => DONE!

    $ rdfcat -out n3 output.ttl prefixes.ttl > catalog.ttl

    $ open catalog.ttl
