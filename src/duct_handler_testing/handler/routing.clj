(ns duct-handler-testing.handler.routing
  (:use [hiccup.core]

        ;; add
        [ring.util.response :only [redirect]])
  (:require
            [clojure.string :as str]
            [duct-handler-testing.boundary.users :as db.users]
            [duct-handler-testing.spec.user :as s.user]
            [integrant.core :as ig]

            ;; add
            [compojure.core :refer [GET POST routes]]
            ;; add2
            [compojure.route :as route]))

(defn error-message-line [err-msg]
  [:div {:style "background: #fcc; margin-bottom: 5px;"} err-msg])

;;============================================================

;; 関数版のほうが、マクロ版よりクォートなどがちょっとだけ少ない。

(defn foo-fn [u param_name err-msgs]
 (let [field# (keyword param_name)]
  `[:div
    [:label {:for ~param_name} ~param_name]
    [:input {:name ~param_name :value ~(field# u)}]
    ~(error-message-line (field# err-msgs))]))

(defmacro foo-mac [u param_name err-msgs]
 (let [field# (keyword param_name)]
  `[:div
    [:label {:for ~param_name} ~param_name]
    [:input {:name ~param_name :value (~field# ~u)}]
    ~(error-message-line `(~field# ~err-msgs))]))

;;============================================================

(defn user-form [action user & {:keys [error-messages]}]
  [:form {:action action :method "post"}

   (foo-fn user "name" error-messages)
   (foo-fn user "email" error-messages)
   (foo-fn user "age" error-messages)

   [:div
    [:a {:href "/users"} "show"]]

   [:button {:type "submit"} "Submit"]])

(defn edit-user-view [user-id user error-messages]
  (html [:div "[ Edit User ]"
         (user-form (str "/users/" user-id "/update") user
                    :error-messages error-messages)]))

(defn new-user-view [user error-messages]
  (html [:div "[ New User ]"
         (user-form "/new" user
                    :error-messages error-messages)]))

(defn show-users-view [users]
  (html [:div
         [:div "[ Users ]"]
         [:a {:href "/new"} "Add user"]
         [:table
          [:thead
           [:tr
            [:th "id"]
            [:th "name"]]]
          [:tbody
           (for [user users]
             [:tr
              [:td [:a {:href (str "/users/" (:id user))} (:id user)]]
              [:td (:name user)]])]]]))

(defn show-user-view [user]
  (html [:div "[ User ]"
         [:div
          (pr-str user)
         [:div
          [:a {:href "/users"} "show"]]
         [:div
          [:a {:href (str "/users/" (:id user) "/edit")} "edit"]
          [:form {:action (str "/users/" (:id user) "/delete") :method "post"}
           [:button {:type "submit"} "delete"]]]]]))

;;============================================================

(defn edit [db id]
  (let [user (first (db.users/get-user db id))]
    (edit-user-view id user nil)))

(defn del [db id]
  (if (= '(1) (db.users/delete-user db id))
    (redirect "/users")
    (let [user (first (db.users/get-user db id))]
      (show-user-view user))))

(defn ins [db {:keys [params]}]
  (if (s.user/valid? params)
    (let [rslt (first (db.users/create-user db params))]
      (redirect (str "/users/" (:generated_key rslt))))
      (new-user-view params (s.user/error-messages params))))

(defn upd [db id {:keys [params]}]
  (if (and
        (s.user/valid? params)
        (= '(1) (db.users/update-user db id params)))
    (redirect (str "/users/" id))
    (edit-user-view id params (s.user/error-messages params))))

;;============================================================

(defmethod ig/init-key :duct-handler-testing.handler/routing [_ {:keys [db]}]
　(routes
   (GET  "/example"          []   (html [:span "This is an example handler"]))
   (GET  "/new"              []   (new-user-view nil nil))
   (POST "/new"              []   #(ins db %))
   (GET  "/users"            []   (show-users-view (db.users/get-users db)))
   (GET  "/users/:id"        [id] (show-user-view (first (db.users/get-user db id))))
   (GET  "/users/:id/edit"   [id] (edit db id))
   (POST "/users/:id/delete" [id] (del db id))
   (POST "/users/:id/update" [id] #(upd db id %))

   ;; switch 文の else のように
   (route/not-found "Not Found")
   ))
