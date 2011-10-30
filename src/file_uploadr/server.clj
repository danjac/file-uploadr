(ns file-uploadr.server
  (:require [file-uploadr.middleware :as middleware]
            [file-uploadr.config :as conf]
            [noir.server :as server])
  (:use somnium.congomongo))


(server/add-middleware middleware/authenticate)
(server/load-views "src/file_uploadr/views/")

(def conn (make-connection (:name conf/db)
                           {:host (or (:host conf/db) "127.0.0.1")}))

(set-connection! conn)


(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'file-uploadr})))

