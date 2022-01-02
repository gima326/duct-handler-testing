(ns duct-handler-testing.boundary.users
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            [duct.database.sql]))

(defprotocol Users
  (get-users [db])
  (get-user [db id])
  (create-user [db params])
  (update-user [db id params])
  (delete-user [db id]))

(extend-protocol Users
  duct.database.sql.Boundary
  (get-users [{:keys [spec]}]
    (jdbc/query spec ["SELECT * FROM users;"]))

  (get-user [{:keys [spec]} id]
   (jdbc/query spec [(format "SELECT * FROM users WHERE id = '%s';" id)]))

  (create-user [{:keys [spec]} params]
   (jdbc/insert! spec :users {:name (:name params) :age (:age params) :email (:email params)}))

  (update-user [{:keys [spec]} id params]
   (jdbc/update! spec :users {:name (:name params) :age (:age params) :email (:email params)} ["id=?" id]))

  (delete-user [{:keys [spec]} id]
   (jdbc/delete! spec :users ["id=?" id])))
