(ns duct-handler-testing.boundary.users
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.core :refer [format]]
            ;;[duct.database.sql]
            ))

(defprotocol Users
  (get-users [db])
  (get-user [db id])
  (create-user [db params])
  (update-user [db id params])
  (delete-user [db id]))

(extend-protocol Users
  ;;duct.database.sql.Boundary
  clojure.lang.PersistentArrayMap

  (get-users [db]
   (jdbc/query db ["SELECT * FROM users;"]))

  (get-user [db id]
   (jdbc/query db [(format "SELECT * FROM users WHERE id = '%s';" id)]))

  (create-user [db params]
   (jdbc/insert! db :users params))

  (update-user [db id params]
   (jdbc/update! db :users params ["id=?" id]))

  (delete-user [db id]
   (jdbc/delete! db :users ["id=?" id])))
