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
