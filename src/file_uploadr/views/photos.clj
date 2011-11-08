(ns file-uploadr.views.photos
  (:require [file-uploadr.views.common :as common]
            [file-uploadr.utils.auth :as auth]
            [file-uploadr.utils.image :as img]
            [file-uploadr.models.users :as users]
            [file-uploadr.models.photos :as photos]
            [noir.validation :as vali]
            [noir.response :as res]
            [noir.session :as session]
            [clojure.string :as string])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers))


(defn upload-is-jpg [file]
  (let [filename (:filename file)]
    (and (not (string/blank? filename))
         (not= (.lastIndexOf (.toLowerCase filename) ".jpg") -1)))) 
    

(defn valid-photo? [{:keys [title file]}]
  (vali/rule (vali/has-value? title)
             [:title "You must include a title"])
  (vali/rule (upload-is-jpg file)
             [:file "Sorry, only JPEGs allowed at this time"])
  (not (vali/errors?)))


(defn to-int [value default]
  (if (nil? value) default
    (try 
      (Integer/parseInt value)
      (catch NumberFormatException default))))



(pre-route [:any "/upload*"] {} (auth/login-required))


(defpartial show-thumbnail [photo-id title thumb]
            (let [url (photos/gen-thumb-url photo-id)
                  path (photos/gen-thumb-path photo-id)
                  [width height] (img/image-size path)]
            [:img {:src url :title title :alt title :width width :height height}]))


(defpartial show-clear [index row-size]
            (if (= (- row-size 1) (mod index row-size)) [:div.clear] ""))

(defpartial show-photo [index {:keys [_id title thumb photo]}]
            (let [photo-url (str "/photo/" _id)]
            (seq [[:div.photo
                    (link-to photo-url (show-thumbnail _id title thumb))]
                   (show-clear index 4)])))


(defpartial render-tag [tag]
            (link-to {:class "tag"} (str "/tag/" tag) tag) "&nbsp;")


(defpartial tag-cloud []
            [:div.tag-cloud.span-12.append-5
             (for [{:keys [_id value]} (photos/tag-cloud)]
               (link-to {:class (str "tag tag-" (int value))} (str "/tag/" _id) _id))])

               
(defpage "/" {:keys [page]}
         (let [page-size 8
               curpage (to-int page 1)]
           (common/layout
             [:h2 "Latest photos"]
             (map-indexed show-photo (photos/latest-photos curpage page-size))
             [:div.clear]
             (common/paginate (fn [page] (url "/" {:page page})) curpage page-size (photos/count-photos))
             (tag-cloud))))


             

 
(defpage "/tag/:tag" {:keys [tag]}
         (common/layout 
           [:h2 "Photos for tag " tag]
           (map-indexed show-photo (photos/photos-by-tag tag))
           [:div.clear]))
           

(defpage "/upload"  {:keys [title description tags]}
           (common/layout
             [:h2 "Upload a photo"]
             (form-to {:enctype "multipart/form-data"}
                      [:post "/upload"]

                      (common/form-field :file "Photo to upload (JPEG only)" 
                                         (file-upload :file))

                      (common/form-field :title "Title"
                                         (text-field :title title))

                      (common/form-field :description "Description"
                                         (text-area :description description))

                      (common/form-field :tags "Tags"
                                         (text-field {:size 50} :tags tags))


                      (common/form-buttons ["Upload"]))))


(defpage [:post "/upload"] {:as photo}
         (if (valid-photo? photo)
           (do 
             (photos/add-photo! (:file photo)
                                (auth/current-user) 
                                (:title photo) 
                                (:description photo) 
                                (:tags photo)) 
             (session/flash-put! "Thanks for your photo!")
             (res/redirect "/"))
           (render "/upload" photo)))

          
(defpage "/photo/:photo-id" {:keys [photo-id]}
         (if-let [photo (photos/get-photo photo-id)]
           (let [url (photos/gen-photo-url photo-id)
                 path (photos/gen-photo-path photo-id)
                 {:keys [title description author author-id tags created]} photo
                 [width height] (img/image-size path)]
             (common/layout 
               [:h2 (:title title)]
               [:h3 (str "uploaded by " author)]
               [:p [:img {:src url
                      :width width
                      :height height
                      :title title
                      :alt title}]]
               [:p.description description]
               [:p.tags (map render-tag tags)]))))




            
