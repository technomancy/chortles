(ns chortles.web
  (:require [ring.adapter.jetty :as jetty]
            [cemerick.drawbridge :as drawbridge]
            [ring.middleware.params :as params]
            [ring.middleware.keyword-params :as keyword-params]
            [ring.middleware.nested-params :as nested-params]
            [ring.middleware.session :as session]
            [ring.middleware.basic-authentication :as basic]))

(defonce scores (atom []))

;; nrepl: inspect HTTP requests
(defonce requests (atom []))

;; nrepl: flush the stored requests list
;; (swap! requests empty).

(defn calculate-chortle-magnitude [chortle]
  (let [sub-chortles (re-seq #"(?im)ha+" chortle)
        caps (apply + (for [sub sub-chortles c sub
                            :when (Character/isUpperCase c)] 1))]
    (+ (count sub-chortles) caps)))

(defn percentile [magnitude]
  (* 100.0 (/ (count (filter (partial >= magnitude) @scores))
              (count @scores))))

(defn app [req]
  (swap! requests conj req)
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

(defn authenticated? [name pass]
  (= [name pass] [(System/getenv "AUTH_USER") (System/getenv "AUTH_PASS")]))

(defn wrap-drawbridge [handler]
  (fn [req]
    (let [handler (if (= "/repl" (:uri req))
                    (basic/wrap-basic-authentication
                     drawbridge-handler authenticated?)
                    handler)]
      (handler req))))

(defn -main [& [port]]
  (let [port (Integer. (or port (System/getenv "PORT")))]
    (jetty/run-jetty (wrap-drawbridge app)
                     {:port port :join? false})))
