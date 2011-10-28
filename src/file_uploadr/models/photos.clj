(ns file-uploadr.models.photos 
  (:require [file-uploadr.utils.db :as db]
            [file-uploadr.utils.image :as img]
            [clojure.string :as string]
            [clojure.contrib.duck-streams :as ds]))


(defn create-photos! []
  (db/create-table!
    :photos
    [:id :integer "PRIMARY KEY" "AUTO_INCREMENT"]
    [:posted :timestamp]
    [:author :integer "NOT NULL"]
    [:title "VARCHAR(200)"]
    [:description :text]
    [:photo "VARCHAR(200)"]
    [:thumb "VARCHAR(200)"]
    [:tags :text]))


(def upload-path "resources/public/img/uploads")

(defn gen-photo-path [photo-id photo]
  (string/join "/" [upload-path "photos" (str photo-id) photo]))


(defn gen-thumb-path [photo-id thumb]
  (string/join "/" [upload-path "thumbs" (str photo-id) thumb]))

(defn gen-thumb-url [photo-id thumb]
  (string/join "/" ["/img" "uploads" "thumbs" (str photo-id) thumb]))


(defn add-photo! [uploaded-file user-id title description tags]
  (let [photo (:filename uploaded-file)
        thumb (str "tn-" photo)
        photo-id (db/insert! :photos {:author user-id 
                                      :title title
                                      :description description
                                      :tags tags
                                      :photo photo
                                      :thumb thumb})
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
        
        ; update photo 
        
        (db/update! :photos ["id=?" photo-id] {:photo photo :thumb thumb})))
      

(defn latest-photos [page limit]
  (db/fetch-all ["SELECT id, title, posted, thumb 
                 FROM photos ORDER BY posted DESC 
                 LIMIT ? OFFSET ?" limit (db/offset page limit)])) 


(defn photo-page-count [page-size]
  (db/page-count (db/fetch-result ["SELECT COUNT(id) FROM photos"]) page-size))
  
