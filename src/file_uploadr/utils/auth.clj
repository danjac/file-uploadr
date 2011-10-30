(ns file-uploadr.utils.auth
  (:require [file-uploadr.middleware :as middleware]
            [noir.response :as res]))


(defn current-user [] middleware/*current-user*)

(defn user-id []
  (user-id (current-user)))

(defn username []
  (:name (current-user)))

(defn logged-in? []
  (not (nil? (current-user))))

(defn login-required []
  (when-not (logged-in?) (res/redirect "/login")))


