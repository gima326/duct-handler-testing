(ns duct-handler-testing.handler.routing-test
  (:require [clojure.test :refer :all]
            [duct-handler-testing.handler.routing :refer :all]

            ;; add
            [ring.mock.request :as mock]
            [duct.core :as duct]
            [integrant.core :as ig]
            [shrubbery.core :as shrubbery]
            [duct-handler-testing.spec.user :as s.user]
            ))

;;============================================================

(def database-stub
  (shrubbery/stub
    ;; プロトコル
    duct-handler-testing.boundary.users/Users
    {
      ;; スタブにする関数名、戻り値
      :get-users [{:id 1 :name "user1" :email "user1@email.com" :age 11}
                  {:id 2 :name "user2" :email "user2@email.com" :age 12}]
      :get-user [{:id 1 :name "user1" :email "user1@email.com" :age 11}]
      :create-user [{:generated_key 3}]
      }))

;;============================================================

(deftest route-test
  (let [hndlr (ig/init-key
                :duct-handler-testing.handler/routing
                {:db database-stub})]

    (testing "route -not-found-"
      (let[{:keys [status, body]}
           (hndlr (mock/request :get "/invalid"))]
        (is (= 404 status))
        (is (= "Not Found" body))))


    (testing "example page exists"
      (let[{:keys [status, body]}
            (hndlr (mock/request :get "/example"))]

        (is (= 200 status))
        (is (= "<span>This is an example handler</span>"
               body))))


    (testing "show-idx page exists"
      (let[{:keys [status, body]}
            (hndlr (mock/request :get "/users"))]

        (is (= 200 status))
        (is (= (str "<div><div>[ Users ]</div><a href=\"/new\">Add user</a>"
                    "<table><thead><tr><th>id</th><th>name</th></tr></thead><tbody>"
                    "<tr><td><a href=\"/users/1\">1</a></td>"
                    "<td>user1</td></tr>"
                    "<tr><td><a href=\"/users/2\">2</a></td>"
                    "<td>user2</td></tr></tbody></table></div>")
               body))
        ))


    (testing "user-show page exists"
      (let[{:keys [status, body]}
            (hndlr (mock/request :get "/users/1"))]

        (is (= 200 status))
        (is (= (str "<div>[ User ]<div>{:id 1, :name \"user1\", :email \"user1@email.com\", :age 11}<div>"
                    "<a href=\"/users\">show</a></div>"
                    "<div><a href=\"/users/1/edit\">edit</a><form action=\"/users/1/delete\" method=\"post\">"
                    "<button type=\"submit\">delete</button></form></div></div></div>")
               body))
        ))


    (testing "user-create page exists"
      (let[{:keys [status, body]}
            (hndlr (mock/request :get "/new"))]

        (is (= 200 status))
        (is (= (str "<div>[ New User ]<form action=\"/new\" method=\"post\">"
                    "<div><label for=\"name\">name</label><input name=\"name\" />"
                    "<div style=\"background: #fcc; margin-bottom: 5px;\"></div></div>"
                    "<div><label for=\"email\">email</label><input name=\"email\" />"
                    "<div style=\"background: #fcc; margin-bottom: 5px;\"></div></div>"
                    "<div><label for=\"age\">age</label><input name=\"age\" />"
                    "<div style=\"background: #fcc; margin-bottom: 5px;\"></div></div>"
                    "<div><a href=\"/users\">show</a></div><button type=\"submit\">Submit</button></form></div>")
               body))
        ))


    (testing "user-create -redirect-"
      (let[body_params {:id 3, :name "user3", :email "user3@email.com", :age "13"}
           {:keys [status, headers, body]}
           (-> (mock/request :post "/new")
               (assoc-in [:params] body_params)
               hndlr)]

        (is (= 302 status))
        (is (empty?  body))
        (is (= {"Location" "/users/3"} headers))
        ))

    (testing "user-create -name empty-"
      (let[body_params {:id 3, :name "", :email "user3@email.com", :age "13"}
           {:keys [status, body]}
           (-> (mock/request :post "/new")
               (assoc-in [:params] body_params)
               hndlr)]

        (is (= 200 status))
        (is (= (str "<div>[ New User ]<form action=\"/new\" method=\"post\">"
            ;; 未入力
            "<div><label for=\"name\">name</label><input name=\"name\" value=\"\" />"
            "<div style=\"background: #fcc; margin-bottom: 5px;\">Please input name.</div></div>"

            "<div><label for=\"email\">email</label><input name=\"email\" value=\"user3@email.com\" />"
            "<div style=\"background: #fcc; margin-bottom: 5px;\"></div></div>"
            "<div><label for=\"age\">age</label><input name=\"age\" value=\"13\" />"
            "<div style=\"background: #fcc; margin-bottom: 5px;\"></div></div>"
            "<div><a href=\"/users\">show</a></div><button type=\"submit\">Submit</button></form></div>")
               body))))

    (testing "user-create -email empty-"
      (let [body_params {:id 3, :name "user3", :email "", :age "13"}
           {:keys [status, body]}
           (-> (mock/request :post "/new")
               (assoc-in [:params] body_params)
               hndlr)]

        (is (= 200 status))
        (is (= (str "<div>[ New User ]<form action=\"/new\" method=\"post\">"
            "<div><label for=\"name\">name</label><input name=\"name\" value=\"user3\" />"
            "<div style=\"background: #fcc; margin-bottom: 5px;\"></div></div>"

            ;; 未入力
            "<div><label for=\"email\">email</label><input name=\"email\" value=\"\" />"
            "<div style=\"background: #fcc; margin-bottom: 5px;\">Please input email.</div></div>"

            "<div><label for=\"age\">age</label><input name=\"age\" value=\"13\" />"
            "<div style=\"background: #fcc; margin-bottom: 5px;\"></div></div>"
            "<div><a href=\"/users\">show</a></div><button type=\"submit\">Submit</button></form></div>")
               body)))
        )

    (testing "user-create -age empty-"
      (let [body_params {:id 3, :name "user3", :email "user3@email.com", :age ""}
           {:keys [status, body]}
           (-> (mock/request :post "/new")
               (assoc-in [:params] body_params)
               hndlr)]

        (is (= 200 status))
        (is (= (str "<div>[ New User ]<form action=\"/new\" method=\"post\">"
            "<div><label for=\"name\">name</label><input name=\"name\" value=\"user3\" />"
            "<div style=\"background: #fcc; margin-bottom: 5px;\"></div></div>"

            "<div><label for=\"email\">email</label><input name=\"email\" value=\"user3@email.com\" />"
            "<div style=\"background: #fcc; margin-bottom: 5px;\"></div></div>"

            ;; 未入力
            "<div><label for=\"age\">age</label><input name=\"age\" value=\"\" />"
            "<div style=\"background: #fcc; margin-bottom: 5px;\">Please input age.</div></div>"
            "<div><a href=\"/users\">show</a></div><button type=\"submit\">Submit</button></form></div>")
               body)))
        )
  ))
