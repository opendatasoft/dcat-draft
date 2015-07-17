(ns ods.transform
  (:require [grafter.rdf.io :as io]
            [clojure.string :as st]))

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

(defn clean-str-str
  [s]
  (boolean (string? (read-string s))))

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
(defmethod parseDate java.util.Date   [x] (.format (java.text.SimpleDateFormat. "yyyy-MM-dd") x))
