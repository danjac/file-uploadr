(ns file-uploadr.models.users
  (:require [noir.util.crypt :as crypt])
  (:use somnium.congomongo))


(defn user-id [user]
  "Create a printable user ID"
  (str (:_id user)))

(defn add-user! [name email password]
  (insert! :users {:name name :email email :password (crypt/encrypt password)}))


(defn get-user [user-id]
  (fetch-one :users :where {:_id (object-id user-id)}))


(defn auth-user [login password]
  (if-let [user (fetch-one :users :where {:$or [{:email login}
                                                {:name login}]})]
    (if (crypt/compare password (:password user)) user)))


