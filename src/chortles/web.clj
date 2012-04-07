(ns chortles.web
  (:require [ring.adapter.jetty :as jetty]))

(defonce scores (atom []))

(defn calculate-chortle-magnitude [chortle]
  (let [sub-chortles (re-seq #"(?im)ha+" chortle)
        caps (apply + (for [sub sub-chortles c sub
                            :when (Character/isUpperCase c)] 1))]
    (+ (count sub-chortles) caps)))

(defn percentile [magnitude]
  (* 100.0 (/ (count (filter (partial >= magnitude) @scores)) (count @scores))))

(defn app [req]
  (let [chortle (slurp (:body req))
        magnitude (calculate-chortle-magnitude chortle)]
    (swap! scores conj magnitude)
    {:status 200
     :headers {"Content-Type" "text/plain"}
     :body (format "{\"%s\": %s, \"percentile\": %s}"
                   chortle magnitude (percentile magnitude))}))

(defn -main [& [port]]
  (let [port (Integer. (or port (System/getenv "PORT")))]
    (jetty/run-jetty #'app {:port port})))
