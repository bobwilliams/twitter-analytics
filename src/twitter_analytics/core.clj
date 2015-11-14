(ns twitter_analytics.core
  (:gen-class)
  (:require [twitter_analytics.models :as models]
            [twitter_analytics.util :as util]
            [clojure.java.io :as io]
            [cheshire.core :as json]
            [twitter.oauth :as oauth]
            [twitter.api.restful :as rest-api]))

(def resource-path (-> "conf.json" io/resource))
(def conf (json/parse-string (slurp resource-path) true))

(defn make-creds [{:keys [consumer-key consumer-secret access-token access-token-secret]}]
  (oauth/make-oauth-creds consumer-key consumer-secret access-token access-token-secret))

(defn find-oldest-id [tweets]
  (->> tweets
       (map :id)
       (reduce min)))

(defn request-timeline [creds params]
  (rest-api/statuses-user-timeline :oauth-creds creds :params params :include_rts 0))

(defn tweets-since [creds user num-tweets id]
  (let [params {:screen-name user :count num-tweets}
        pararms-with-max-id (if id (assoc params :max_id id) params)
        response (request-timeline creds pararms-with-max-id)]
    (:body response)))

(defn retrieve-tweet-feed [creds user num-tweets]
  (loop [tweets [] last-id nil]
    (let [new-tweets (tweets-since creds user num-tweets last-id)
          oldest-id (find-oldest-id new-tweets)
          tweets-so-far (concat tweets new-tweets)]
      (if (< (count new-tweets) num-tweets)
        tweets-so-far
        (recur tweets-so-far (dec oldest-id))))))

(defn get-data [creds screen-name num-tweets]
  (->> (retrieve-tweet-feed creds screen-name num-tweets)
       (map :text)))

(defn process-data [data]
  (let [cap (conf :stats-cap)]
    (->> data
      (flatten)
      (group-by identity)
      (map (fn [[k v]] [(util/key-it k) (count v)]))
      (sort-by (fn [[k v]] v))
      (reverse)
      (take cap)
      (into {}))))

(defn -main [& args]
  (let [creds (make-creds conf)
        num-tweets (conf :num-tweets-to-fetch)
        screen-name (conf :screen-name)
        data (get-data creds screen-name num-tweets)
        tokens (map models/tokenize data)
        twitter-handles   {:handles (process-data (map util/extract-users data))} 
        ner-person        {:names (process-data (map models/entity-person-find tokens))}
        ner-locations     {:locations (process-data (map models/entity-location-find tokens))}
        ner-organizations {:organizations (process-data (map models/entity-organization-find tokens))}
        ner-time          {:times (process-data (map models/entity-time-find tokens))}]
        
        [twitter-handles ner-person ner-locations ner-organizations ner-time]))
