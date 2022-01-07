# duct-handler-testing

Webフレームワーク「Duct」入門サンプルコード、単体テスト編。

Compojure によるルーティングをテストしてます。<br>
テスト用の DB アクセスのモックを作成するのに shrubbery を使っています。<br>

当初は migration を使ってやろうとしたけれど、うまくいかなかった。<br>
…なので、とりあえず試行錯誤して現状動くやつをば。<br>


## Developing

<!---

### Setup

When you first clone this repository, run:

```sh
lein duct setup
```

This will create files for local configuration, and prep your system
for the project.

### Environment

To begin developing, start with a REPL.

```sh
lein repl
```

Then load the development environment.

```clojure
user=> (dev)
:loaded
```

Run `go` to prep and initiate the system.

```clojure
dev=> (go)
:duct.server.http.jetty/starting-server {:port 3000}
:initiated
```

By default this creates a web server at <http://localhost:3000>.

When you make changes to your source files, use `reset` to reload any
modified files and reset the server.

```clojure
dev=> (reset)
:reloading (...)
:resumed
```

-->

### Testing

Testing is fastest through the REPL, as you avoid environment startup
time.

```clojure
dev=> (test)
...
```

But you can also run tests (with keywords) through Leiningen.

```edn
;; [  duct-handler-testing/project.clj ]

(defproject duct-handler-testing "0.1.0-SNAPSHOT"

 (中略)

 :profiles
  {:dev  [:project/dev :profiles/dev]
   :repl {:prep-tasks   ^:replace ["javac" "compile"]
          :repl-options {:init-ns user}}
   :uberjar {:aot :all}
   :profiles/dev {}
   :project/dev  {:source-paths   ["dev/src"]
                  :resource-paths ["dev/resources"]
                  :dependencies   [[integrant/repl "0.3.2"]
                                   [hawk "0.2.11"]
                                   [eftest "0.5.9"]

                                   ;; add
                                   [phrase "0.3-alpha4"]
                                   [ring/ring-mock "0.3.2"]
                                   [com.gearswithingears/shrubbery "0.4.1"]]
                  ;; add2
                  ;; テストコードに追加したキーワード（:static など）と一致させること。
                  ;; キーワードに該当するテストのみ、部分的に実行できる。
                  :test-selectors {:s :static :d :dynamic}
                  }})
```

```sh
$ lein test :s
Java HotSpot(TM) 64-Bit Server VM warning: 
Options -Xverify:none and -noverify were deprecated in JDK 13 and will likely be removed in a future release.

lein test duct-handler-testing.handler.routing-test

Ran 1 tests containing 4 assertions.
0 failures, 0 errors.

```

<!---

## Legal

Copyright © 2021 FIXME

-->

## References

- 「[clojureのductでcrudアプリを作る方法][1]」 [ `https://asukiaaa.blogspot.com/2017/12/clojureductcrud.html` ]<br>
- 「[Clojure と Duct の単体テスト][2]」 [ `https://webcache.googleusercontent.com/search?q=cache:wyR4h6_n7VAJ:https://sfkd.hatenablog.com/+&cd=1&hl=ja&ct=clnk&gl=jp&client=firefox-b-d` ]<br>

[1]: https://asukiaaa.blogspot.com/2017/12/clojureductcrud.html
[2]: https://webcache.googleusercontent.com/search?q=cache:wyR4h6_n7VAJ:https://sfkd.hatenablog.com/+&cd=1&hl=ja&ct=clnk&gl=jp&client=firefox-b-d
