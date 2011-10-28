(ns file-uploadr.utils.db 
  (:require [clojure.java.jdbc :as jdbc]))


(def db {:classname "com.mysql.jdbc.Driver"
         :subprotocol "mysql"
         :subname "//localhost:3306/uploadr"
         :user "root"
         :password "m0nk3Y"})


(defmacro with-db [& body]
  `(jdbc/with-connection db ~@body))


(defn fetch-all [query]
  "Returns results of query as vector"
  (with-db
    (jdbc/with-query-results res query
                             (doall res))))



(defn fetch-one [query]
  "Returns results of first row only"
  (first (fetch-all query)))
  

(defn fetch-result [query]
  "Returns first column of first row"
  (first (vals (fetch-one query))))
  

(defn insert! [tablename values]
  (with-db
    (:generated_key (first (jdbc/insert-records tablename values)))))


(defn update! [tablename where values]
  (with-db
    (jdbc/update-values tablename where values)))


(defn delete! [tablename where]
  (with-db
    (jdbc/delete-rows tablename where)))


(defn create-table! [& args]
  (with-db
    (apply jdbc/create-table args)))


(defn offset [page limit]
  (* limit (- (int page) 1)))


(defn page-count [page-size total-rows]
  (int (Math/ceil (/ total-rows page-size))))


