(ns file-uploadr.views.upload
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
         (not= (.lastIndexOf filename ".jpg") -1)))) 
    

(defn valid-photo? [{:keys [title file]}]
  (vali/rule (vali/has-value? title)
             [:title "You must include a title"])
  (vali/rule (upload-is-jpg file)
             [:file "Sorry, only JPEGs allowed at this time"])
  (not (vali/errors?)))


(pre-route "/upload*" {} (auth/login-required))

(defpartial form-error [[error]]
            [:div.error error])


(defpartial show-thumbnail [photo-id title thumb]
            (let [url (photos/gen-thumb-url photo-id thumb)
                  path (photos/gen-thumb-path photo-id thumb)
                  [width height] (img/image-size path)]
            [:img {:src url :title title :alt title :width width :height height}]))


(defpartial show-photo [{:keys [id title thumb photo]}]
            [:div.photo.span-6 
             [:h3 title]
             (show-thumbnail id title thumb)])


(defpage "/" []
           (common/layout
             [:h2 "Latest photos"]
             (if (auth/logged-in?)
               [:p (link-to "/upload" "Upload a photo")])
             (map show-photo (photos/latest-photos 1 4))))
             

(defpage "/login" []
         (common/layout
           [:h2 "Sign into FileUploadr"]
           (form-to [:post "/login"]
                    [:div.field 
                      (label :login "Username or email address") [:br]
                      (text-field :login)]
                    [:div.field
                      (label :password "Your password") [:br]
                      (password-field :password)]
                    [:div.buttons
                      (submit-button "Sign in")])))


(defpage [:post "/login"] {:keys [login password]}
         (if-let [user (users/authenticate login password)]
           (do 
              (session/put! :user-id (:id user))
              (res/redirect "/"))
           (render "/login")))

(defpage "/logout" []
         (session/remove! :user-id)
         (res/redirect "/"))
  

(defpage "/upload"  {:keys [title description tags]}
           (common/layout
             [:h2 "Upload a photo"]
             (form-to {:enctype "multipart/form-data"}
                      [:post "/upload"]
                      [:div.field
                        (vali/on-error :file form-error)
                        (label :file "Photo to upload (JPEGs only!)") [:br]
                        (file-upload :file)]
                      [:div.field
                        (vali/on-error :title form-error)
                        (label :title "Title") [:br]
                        (text-field :title title)]
                      [:div.field
                        (label :description "Description") [:br]
                        (text-area :description description)]
                      [:div.field
                        (label :tags "Tags") [:br]
                        (text-field {:size 50} :tags tags)]
                      [:div.buttons
                        (submit-button "Upload")])))


(defpage [:post "/upload"] {:as photo}
         (if (valid-photo? photo)
           (do 
             (photos/add-photo! (:file photo)
                                (auth/user-id) 
                                (:title photo) 
                                (:description photo) 
                                (:tags photo)) 
             (res/redirect "/"))
           (render "/upload" photo)))

          

            
