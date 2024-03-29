(ns file-uploadr.views.auth
  (:require [file-uploadr.views.common :as common]
            [file-uploadr.utils.auth :as auth]
            [file-uploadr.models.users :as users]
            [noir.validation :as vali]
            [noir.response :as res]
            [noir.session :as session])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers
        hiccup.form-helpers))


(defpage "/login" []
         (common/layout
           [:h2 "Sign into PhotoBook"]
           (form-to [:post "/login"]
                    (common/form-field :login "Username or email address"
                                       (text-field :login))
                    (common/form-field :password "Your password"
                                       (password-field :password))
                    (common/form-buttons ["Sign in"]))))


(defpage [:post "/login"] {:keys [login password]}
         (if-let [user (users/auth-user login password)]
           (do 
             (session/put! :user-id (users/user-id user))
             (session/flash-put! (str "Welcome back, " (:name user)))
             (res/redirect "/"))
           (do
             (session/flash-put! "Sorry, not the right login stuff") 
             (render "/login"))))

(defpage "/logout" []
         (session/remove! :user-id)
         (res/redirect "/"))
 
