(ns file-uploadr.models.photos 
  (:require [file-uploadr.utils.db :as db]
            [file-uploadr.utils.image :as img]
            [file-uploadr.config :as conf]
            [clojure.string :as string]
            [clojure.contrib.duck-streams :as ds])
  (:use somnium.congomongo))


(defn gen-photo-path [photo-id photo]
  (string/join "/" [conf/upload-path "photos" (str photo-id) photo]))


(defn gen-thumb-path [photo-id thumb]
  (string/join "/" [conf/upload-path "thumbs" (str photo-id) thumb]))

(defn gen-thumb-url [photo-id thumb]
  (string/join "/" ["/img" "uploads" "thumbs" (str photo-id) thumb]))


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
                                   :created created
                                   :thumb thumb})
        photo-id (str (:_id document))
        photo-path (gen-photo-path photo-id photo)
        thumb-path (gen-thumb-path photo-id thumb)
        photo-file (ds/file-str photo-path)
        thumb-file (ds/file-str thumb-path)]

        ; make parent dirs
        (ds/make-parents photo-file)
        (ds/make-parents thumb-file)

        ; create thumbnail
        (ds/copy (:tempfile uploaded-file) photo-file)
        (img/create-thumbnail photo-path thumb-path 200 200)

        (update! :photos document (merge document { :photo photo :thumb thumb })) photo-id))

(defn latest-photos [page limit]
    (fetch :photos 
           :limit limit 
           :skip (db/offset page limit) 
           :sort { :created -1 } ))

(defn page-count [page-size]
  (db/page-count (fetch-count :photos) page-size))
