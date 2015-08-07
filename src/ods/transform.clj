(ns ods.transform
  (:require [grafter.rdf.io :as io]
            [clojure.string :as st]
            [ods.prefix :refer [dcat:keyword]]))

(defn s [s] (if (seq s) (io/s s) ""))

(defn trim [s] (if (seq s) (st/trim s) ""))

(defn lower-case [s] (if (seq s) (st/lower-case s) ""))

(defn capitalize [s] (if (seq s) (-> s trim st/capitalize) ""))

(defn titleize
  "Capitalizes each word in a string"
  [s]
  (when (seq s)
    (let [a (st/split s #" ")
          c (map st/capitalize a)]
      (->> c (interpose " ") (apply str) trim))))

(defn remove-blanks
  "Removes blanks in a string"
  [s]
  (when (seq s)
    (st/replace s " " "")))

(defn title-slug
  "String -> PascalCase"
  [s]
  (when (seq s)
    (-> s
        titleize
        remove-blanks)))

(defn slugify
  "Cleans and slugifies string"
  [string]
  (if-not (empty? string)
    (-> string
        st/trim
        (st/lower-case)
        (st/replace "\"" "")
        (st/replace "(" "-")
        (st/replace ")" "")
        (st/replace "  " "")
        (st/replace "," "-")
        (st/replace "." "")
        (st/replace " " "-")
        (st/replace "/" "-")
        (st/replace "'" "")
        (st/replace "---" "-")
        (st/replace "--" "-"))))

(comment (defn clean-str-str
           [s]
           (read-string s)))

(defn ->slug
  "Slugifies several strings"
  [& args]
  (let [args (map slugify args)
        a (map #(when (seq %) (st/trim %)) args)
        f (remove nil? a)]
    (apply str (interpose "-" f))))

(defn unslug
  "Opposite of slufigy"
  [s]
  (when (seq s)
    (let [a (st/split s #"-")]
      (apply str a))))

(defn unsnake
  "Same as un slug for snake-cased slugs"
  [s]
  (when (seq s)
    (let [a (st/split s #"_")]
      (apply str (interpose " " a)))))

(defn slug-combine
  "Combines slugs to create URI"
  [& args]
  (apply str (interpose "/" args)))

(defn urify-uri
  [string]
  (when (seq string)
    (if (= \h (first string))
      (if (re-find #"\[" string) (s string) string)
      (str "http://" string))))

;; Parser

(defmulti parseValue class)
(defmethod parseValue :default             [x] x)
(defmethod parseValue nil                  [x] nil)
(defmethod parseValue java.lang.Character  [x] (Character/getNumericValue x))
(defmethod parseValue java.lang.String     [x] (if (= "" x)
                                                nil
                                                (if (.contains x ".")
                                                  (Double/parseDouble x)
                                                  (Integer/parseInt x))))
(defmethod parseValue java.math.BigDecimal [x] (Double/parseDouble (str x)))

;; Dates

(defn date->yyyy-mm-dd
  "Converts java.util.Date to yyyy-mm-dd"
  [date]
  (when-not (nil? date)
    (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") date)))

(defn s->yyyy-mm-dd
  "Converts dd/mm/yyyy to yyyy-mm-dd"
  [s]
  (when (seq s)
    (let [[d m y] (st/split s #"/")]
      (apply str (interpose "-" [y m d])))))

(defmulti parseDate class)
(defmethod parseDate nil              [x] nil)
(defmethod parseDate java.lang.String [x] (s->yyyy-mm-dd x))
(defmethod parseDate java.util.Date   [x] (.format (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ss.SSSXXX") x))

(def licenses {"odbl-paris" "http://opendata.paris.fr/page/lalicence/"
               "open-database-license-odbl" "http://opendatacommons.org/licenses/odbl/summary/"
               "la-réutilisation-de-geofla®-est-gratuite-pour-tous-les-usages-y-compris-commerciaux-selon-les-termes-de-la-licence-ouverte-version-10" "https://wiki.data.gouv.fr/images/9/9d/Licence_Ouverte.pdf"
               "http:-wwwdatagouvfr-licence-ouverte-open-licence" "https://wiki.data.gouv.fr/images/9/9d/Licence_Ouverte.pdf"
               "odbl" "http://opendatacommons.org/licenses/odbl/summary/"
               "licence-ouverte-open-licence" "https://wiki.data.gouv.fr/images/9/9d/Licence_Ouverte.pdf"
               "open-data-paris" "http://opendata.paris.fr/page/lalicence/"
               "cc-by-sa" "https://creativecommons.org/licenses/by-sa/2.0/"
               "public-domain" "https://en.wikipedia.org/wiki/Public_domain"
               "licence-ouverte-etalab" "https://wiki.data.gouv.fr/images/9/9d/Licence_Ouverte.pdf"
               "netatmo" "https://www.netatmo.com/site/terms"
               "insee" "http://www.insee.fr/fr/service/default.asp?page=rediffusion/rediffusion.htm"
               "licence-ouverte-10-etalab" "https://wiki.data.gouv.fr/images/9/9d/Licence_Ouverte.pdf"
               "cc-by" "https://creativecommons.org/licenses/by/2.0/"
               "licence-ouverte-datagouvfr" "https://wiki.data.gouv.fr/images/9/9d/Licence_Ouverte.pdf"})

(defn ->license
  [s]
  (when (seq s)
    (if (or (re-find #"http://" s)
            (re-find #"https://" s))
      s
      (-> s
          slugify
          licenses))))

(def themes
  {"Environnement" "Environnement"
   "Urbanisme" "Aménagement du territoire, Urbanisme, Bâtiments, Equipements, Logement"
   "Education" "Education, Formation, Recherche, Enseignement"
   "Health" "Santé"
   "Permitting" "Permitting"
   "Citoyens" "Administration, Gouvernement, Finances publiques, Citoyenneté"
   "Service" "Services, Social"
   "Restaurants" "Hébergement, Restauration"
   "Transportation" "Transports, Déplacements"
   "Territoire" "Aménagement du territoire, Urbanisme, Bâtiments, Equipements, Logement"
   "Culture" "Culture, Patrimoine"
   "Services" "Services, Social"
   "Public" "Administration, Gouvernement, Finances publiques, Citoyenneté"
   "Sport" "Sport, Loisirs"
   "Sécurité" "Justice, Sécurité, Police, Crime"
   "Déplacements" "Transports, Déplacements"
   "Finances" "Administration, Gouvernement, Finances publiques, Citoyenneté"
   "Transport" "Transports, Déplacements"
   "Spatial" "Spatial"
   "Transports" "Transports, Déplacements"
   "Justice" "Justice, Sécurité, Police, Crime"
   "Aménagement" "Aménagement du territoire, Urbanisme, Bâtiments, Equipements, Logement"
   "Santé" "Santé"
   "Administration" "Administration, Gouvernement, Finances publiques, Citoyenneté"
   "Environment" "Environnement"
   "Economie" "Economie, Business, PME, Développement économique, Emploi"})

(defn ->theme
  [s]
  (when (seq s)
    (let [w (re-seq #"\p{L}+" s)]
      (-> w
          first
          themes))))

(def publisher-uris
  {"Ville de Paris" "http://opendata.paris.fr"
   "Direction des Espaces Verts et de l'Environnement (DEVE) - Ville de Paris" "http://opendata.paris.fr/"
   "Ville de Paris - Direction de la propreté et de l'eau" "http://opendata.paris.fr/"
   "BOSTON/Boston Public Schools" "https://data.cityofboston.gov/"
   "NYC/Department of Education (DOE)" "http://schools.nyc.gov"
   "BOSTON/Mayor's Office of Food Initiatives" "https://data.cityofboston.gov/"
   "BOSTON/Inspectional Services Dept." "https://data.cityofboston.gov/"
   "IGN" "http://www.ign.fr/"
   "Etalab" "http://data.gouv.fr"
   "La Poste" "https://www.data.gouv.fr/fr/organizations/la-poste/"
   "OpenDataSoft" "http://data.opendatasoft.com"
   "NYC/Department of Information Technology & Telecommunications (DoITT)" "http://www.nyc.gov/html/doitt/html/home/home.shtml"
   "Département de Maine-et-Loire" "http://www.maine-et-loire.fr/"
   "NYC/Department of Consumer Affairs (DCA)" "http://www1.nyc.gov/site/dca/index.page"
   "OpenStreetMap" "http://www.openstreetmap.org"
   "USA/Office of Elementary and Secondary Education" "http://schools.nyc.gov/default.htm"
   "INSEE" "http://www.insee.fr/fr/"
   "NYC/Fire Department of New York City (FDNY)" "http://www.nyc.gov/html/fdny/html/home2.shtml"
   "FIFA" "http://www.fifa.com/"
   "Ministère de l'Intérieur" "http://www.interieur.gouv.fr/"
   "Assemblée nationale / NosDéputés.fr / Regards Citoyens" "http://www.nosdeputes.fr/"
   "AutoLib" "https://www.autolib.eu/fr/"
   "Direction de la Voirie et des déplacements - Agence de la mobilité" "http://opendata.paris.fr"
   "JCDecaux" "https://developer.jcdecaux.com/#/home"
   "OpenAgenda" "https://openagenda.com/"
   "Natural Earth" ""
   "European Institute for Gender Equality" "http://eige.europa.eu/"
   "Ministère des finances et des comptes publics" "http://www.economie.gouv.fr/les-ministeres/directions-ministere-finances-comptes-publics"
   "ministère de l'Economie, de l'Industrie et du Numérique" "http://www.economie.gouv.fr"
   "The World Bank" "http://data.worldbank.org/"
   "Chicago Police Department" "https://data.cityofchicago.org"
   "Ministère de l'Écologie, du Développement durable et de l'Énergie" "http://www.developpement-durable.gouv.fr/"
   "Wikipedia Contributors" "https://fr.wikipedia.org"
   "CNAM" "http://www.ameli.fr/"
   "Saint-Malo Agglomération" "http://www.ville-saint-malo.fr/"
   "Haute Autorité de Santé" "http://www.has-sante.fr/portail/"
   "Assemblée Nationale" "https://www.data.gouv.fr/fr/organizations/assemblee-nationale/"
   "Réseau des bibliothèques de prêt de la Ville de Paris" "http://opendata.paris.fr"
   "Capitaine Train" "https://www.capitainetrain.com/"
   "Helsinki Urban Facts" "http://www.hel.fi/www/tieke/en"
   "City of Helsinki City Planning Department" "http://www.hel.fi/www/tieke/en"
   "Data Grand Lyon" "http://data.grandlyon.com/"
   "data.gouv.fr" "http://data.gouv.fr"
   "Data Publica" "http://www.data-publica.com/"
   "Rennes Métropole" "http://metropole.rennes.fr/"
   "Ville de Bordeaux" "http://www.bordeaux.fr/"
   "Social Security Administration" "http://www.ssa.gov/"
   "Ville de Montpellier" "http://www.montpellier.fr/"
   "Eurométropole de Strasbourg" "http://www.strasbourg.eu/"
   "Marseille Provence Métropole" "http://www.marseille.fr/sitevdm/jsp/site/Portal.jsp"
   "Métropole Nice Côte d'Azur" "http://www.nicecotedazur.org/"
   "Nantes Métropole" "http://www.nantesmetropole.fr/"})
