(ns file-uploadr.models.photos 
  (:require [file-uploadr.utils.db :as db]
            [file-uploadr.utils.image :as img]
            [file-uploadr.config :as conf]
            [clojure.string :as string]
            [clojure.contrib.duck-streams :as ds])
  (:use somnium.congomongo))


(defn photo-filename [photo-id]
  (str photo-id ".jpg"))

(defn thumb-filename [photo-id]
  (str "tn-" photo-id ".jpg"))

(defn gen-photo-path [photo-id]
  (string/join "/" [conf/upload-path "photos" (photo-filename photo-id)]))


(defn gen-thumb-path [photo-id]
  (string/join "/" [conf/upload-path "thumbs" (thumb-filename photo-id)]))


(defn gen-photo-url [photo-id]
  (string/join "/" ["/img" "uploads" "photos" (photo-filename photo-id)]))

(defn gen-thumb-url [photo-id]
  (string/join "/" ["/img" "uploads" "thumbs" (thumb-filename photo-id)]))


(defn add-photo! [uploaded-file user title description tags]
  (let [photo (:filename uploaded-file)
        thumb (str "tn-" photo)
        created (new java.util.Date)
        document (insert! :photos {:author (:name user)
                                   :author-id (:_id user)
                                   :title title
                                   :description description
                                   :tags tags
                                   :photo photo
                                   :created created})
        photo-id (str (:_id document))
        photo-path (gen-photo-path photo-id)
        thumb-path (gen-thumb-path photo-id)
        photo-file (ds/file-str photo-path)
        thumb-file (ds/file-str thumb-path)]

        ; make parent dirs
        (ds/make-parents photo-file)
        (ds/make-parents thumb-file)

        ; create thumbnail
        (ds/copy (:tempfile uploaded-file) photo-file)
        (img/create-thumbnail photo-path thumb-path 200 200)
        (img/create-thumbnail photo-path photo-path 1000 1000)

        photo-id))

(defn get-photo [photo-id]
  (fetch-one :photos :where {:_id (object-id photo-id)}))

(defn latest-photos [page limit]
    (fetch :photos 
           :limit limit 
           :skip (db/offset page limit) 
           :sort { :created -1 } ))

(defn count-photos [] (fetch-count :photos))
