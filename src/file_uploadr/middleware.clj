(ns file-uploadr.middleware
    (:require [noir.session :as session]
              [file-uploadr.models.users :as users]))


(declare ^{:dynamic true} *current-user*)

(defn session-user []
    (if-let [user-id (session/get :user-id)]
      (users/get-user user-id)))


(defn authenticate [handler]
  "Looks up user ID in session, pulls data from DB into session"
    (fn [req]
        (binding [*current-user* (session-user)]
        (let [response (handler req)] response))))


