(ns twitter_analytics.core
  (:require [twitter_analytics.models :as models]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.pprint :as pprint]
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
      (if (< (count new-tweets) 200)
        tweets-so-far
        (recur tweets-so-far (dec oldest-id))))))

(defn extract-users [text]
  (->> text
       (re-seq #"@(\w+)")
       (map (fn [[x y]] y))
       (map str/lower-case)))

(defn sentencize [sentence] 
  (str sentence "."))

(defn get-data [creds screen-name num-tweets]
  (->> (retrieve-tweet-feed creds screen-name num-tweets)
       (map :text)
       (map sentencize)))

(defn process-data [data]
  (->> data
    (flatten)
    (group-by identity)
    (map (fn [[k v]] [k (count v)]))
    (sort-by (fn [[k v]] v))
    (reverse)
    (take 10)))

(defn -main [& args]
  (let [creds (make-creds conf)
        num-tweets (conf :num-tweets-to-fetch)
        screen-name (conf :screen-name)
        data (get-data creds screen-name num-tweets)
        tokens (map models/tokenize data)
        twitter-handles   {:Handles (process-data (map extract-users data))} 
        ner-person        {:Names (process-data (map models/entity-person-find tokens))}
        ner-locations     {:Locations (process-data (map models/entity-location-find tokens))}
        ner-organizations {:Organizations (process-data (map models/entity-organization-find tokens))}
        ner-time          {:Times (process-data (map models/entity-time-find tokens))}]
        
        [twitter-handles ner-person ner-locations ner-organizations ner-time]))
