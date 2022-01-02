(ns duct-handler-testing.spec.user
  (:require [clojure.spec.alpha :as s]
            [phrase.alpha :refer [phrase defphraser phrase-first]]))

(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")
(defn email-format? [email] (re-matches email-regex email))

(s/def ::email
  (s/and not-empty string? email-format?))

(defphraser not-empty
  {:via [::email]}
  [_ _]
  "Please input email.")

(defphraser string?
  {:via [::email]}
  [_ _]
  "Please input email as string.")

(defphraser email-format?
  {:via [::email]}
  [_ _ _]
  "Please input as email format.")

;;==============

(s/def ::age not-empty)

(defphraser not-empty
  {:via [::age]}
  [_ _]
  "Please input age.")

;;==============

(s/def ::name not-empty)

(defphraser not-empty
  {:via [::name]}
  [_ _]
  "Please input name.")

;;==============

(s/def ::user (s/keys :req [::name ::age ::email]))

(defn user->spec-user [user]
  {::name (:name user)
   ::age (:age user)
   ::email (:email user)})

(defn spec-user->user [spec-user]
  {:name (::name spec-user)
   :age (::age spec-user)
   :email (::email spec-user)})

(defn valid? [user]
  (s/valid? ::user (user->spec-user user)))

(defn error-message [user]
  (phrase-first {} ::user (user->spec-user user)))

(defn error-messages [user]
  (->>
   (for [problem (some->> (user->spec-user user)
                          (s/explain-data ::user)
                          (::s/problems))]
     (let [message (phrase {} problem)
           path (first (:path problem))]
       [path message]))
   (into {})
   spec-user->user))
