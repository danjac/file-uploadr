(ns file-uploadr.views.common
  (:require [file-uploadr.utils.auth :as auth])
  (:use noir.core
        hiccup.core
        hiccup.page-helpers))

(defpartial layout [& content]
            (html5
              [:head
               [:title "file-uploadr"]
               (include-css "/css/blueprint/screen.css")]
              [:body
               [:div.container
                [:h1 "FileUploadr"]
                [:div.span-24.navigation
                  (if (auth/logged-in?)
                    (link-to "/logout" (str "Sign out, " (auth/username)))
                    (link-to "/login" "Sign in"))]
                [:div.span-24.content content]]]))
