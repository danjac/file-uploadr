(ns file-uploadr.models.users
  (:require [file-uploadr.utils.db :as db]
            [noir.util.crypt :as crypt]))


(defn create-users! []
  "Create users table"
  (db/create-table!
    :users
    [:id :integer "PRIMARY KEY" "AUTO_INCREMENT"]
    [:name "VARCHAR(50)" "UNIQUE"]
    [:password "VARCHAR(60)"]
    [:email "VARCHAR(150)" "UNIQUE"]
    [:joined :timestamp]))


(defn add-user! [name email password]
  (db/insert! :users {:name name :email email :password (crypt/encrypt password)}))

(defn delete-user! [user-id]
  (db/delete! :users ["id=?" user-id]))


(defn get-user [user-id]
  (db/fetch-one ["SELECT * FROM users WHERE id=?" user-id]))

(defn authenticate [login password]
  (if-let [user (db/fetch-one ["SELECT * FROM users WHERE email=? OR name=?" login login])]
    (if (crypt/compare password (:password user)) user)))


