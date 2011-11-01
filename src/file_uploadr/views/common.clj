(ns file-uploadr.views.common
  (:require [file-uploadr.utils.auth :as auth]
            [noir.session :as session]
            [noir.validation :as vali])
  (:use noir.core
        hiccup.core
        hiccup.form-helpers
        hiccup.page-helpers))

(defpartial layout [& content]
            (html5
              [:head
               [:title "PhotoBook"]
               (include-css "/css/blueprint/screen.css")
               (include-css "/css/file_uploadr.css")]
              [:body
               [:div.container
                [:header.span-24
                  [:h1.span-12 "PhotoBook"]
                  [:nav.span-12.last
                   [:ul]
                    [:li (link-to "/" "Home")]
                    (if (auth/logged-in?)
                      (seq [[:li (link-to "/logout" (str "Sign out, " (auth/username)))]
                            [:li (link-to "/upload" "Upload a photo")]])
                      [:li (link-to "/login" "Sign in")])]]
                (if-let [flash-message (session/flash-get)]
                  [:div.span-24.notice flash-message])
                [:div.span-24.content content]]]))


(defpartial form-error [[error]]
            [:div.error error])


(defpartial form-field [field label-text & body]
            [:div.field
             (vali/on-error field form-error)
             (label field label-text) [:br] body])


(defpartial form-buttons [buttons]
            [:div.buttons
            (map (fn ([button] (submit-button button))) buttons)])


(defpartial paginate [url-fn curpage page-size total]
            (let [num-pages (int (Math/ceil (/ total page-size)))]
              (if (> num-pages 1)
                [:nav.pagination
                (for [page (range 1 (+ num-pages 1))]
                  [:li
                  (if (= page curpage)
                    [:b page]
                    (link-to (url-fn page) page))])])))


