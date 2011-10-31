(ns file-uploadr.views.test_upload
  (:require [noir.session :as session])
  (:use clojure.test
        noir.util.test))


(def hello "hello world")

(deftest simple-test
    (is (= hello "hello world")))

(deftest index-test-not-logged-in
    (with-noir 
      (is (nil? session/get :user-id))))

