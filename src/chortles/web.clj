(ns chortles.web
  (:require [ring.adapter.jetty :as jetty]
            [cemerick.drawbridge :as drawbridge]
            [ring.middleware.params :as params]
            [ring.middleware.keyword-params :as keyword-params]
            [ring.middleware.nested-params :as nested-params]
            [ring.middleware.session :as session]
            [remvee.ring.middleware.basic-authentication :as basic]))

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
     :body (format "{\"%s\": %s, \"percentile\": %.2f}"
                   chortle magnitude (percentile magnitude))}))

(def drawbridge-handler
  (-> (cemerick.drawbridge/ring-handler)
      (keyword-params/wrap-keyword-params)
      (nested-params/wrap-nested-params)
      (params/wrap-params)
      (session/wrap-session)))

(defn wrap-drawbridge [handler]
  (fn [req]
    (if (= "/repl" (:uri req))
      (drawbridge-handler req)
      (handler req))))

(defn -main [& [port]]
  (let [port (Integer. (or port (System/getenv "PORT")))]
    (jetty/run-jetty (wrap-drawbridge app)
                     {:port port :join? false})))
