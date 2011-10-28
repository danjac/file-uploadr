(ns file-uploadr.server
  (:require [file-uploadr.middleware :as middleware]
            [noir.server :as server]))

(server/add-middleware middleware/authenticate)
(server/load-views "src/file_uploadr/views/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (server/start port {:mode mode
                        :ns 'file-uploadr})))

