(defproject chortles "1.0.0"
  :description "Just for laughs"
  :url "http://chortles.herokuapp.com"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-jetty-adapter "1.1.6"]
                 [com.cemerick/drawbridge "0.0.6"]
                 [ring-basic-authentication "1.0.1"]]
  :min-lein-version "2.0.0"
  :main chortles.web)
