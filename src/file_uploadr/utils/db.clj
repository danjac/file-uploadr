(ns file-uploadr.utils.db 
  (:require [file-uploadr.config :as conf])
  (:use somnium.congomongo))

(def conn (make-connection (:name conf/db)
                           {:host (or (:host conf/db) "127.0.0.1")}))


(defn connect []
  (set-connection! conn))


(defn offset [page limit]
  (* limit (- (int page) 1)))


(defn page-count [page-size total-rows]
  (int (Math/ceil (/ total-rows page-size))))


