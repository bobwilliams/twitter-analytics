(ns twitter_analytics.core
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [cheshire.core :as json]
            [twitter.oauth :as oauth]
            [twitter.api.restful :as rest-api]
            [opennlp.nlp :as nlp]))

(def resource-path (-> "conf.json" io/resource))
(def named-entity-person-path (-> "models/en-ner-person.bin" io/resource))
(def named-entity-organization-path (-> "models/en-ner-organization.bin" io/resource))
(def named-entity-location-path (-> "models/en-ner-location.bin" io/resource))
(def named-entity-money-path (-> "models/en-ner-money.bin" io/resource))
(def named-entity-time-path (-> "models/en-ner-time.bin" io/resource))
(def named-entity-percentage-path (-> "models/en-ner-percentage.bin" io/resource))
(def tokenizer-path (-> "models/en-token.bin" io/resource))

(def tokenize (nlp/make-tokenizer tokenizer-path))
(def entity-person-find (nlp/make-name-finder named-entity-person-path))
(def entity-location-find (nlp/make-name-finder named-entity-location-path))
(def entity-organization-find (nlp/make-name-finder named-entity-organization-path))
(def entity-time-find (nlp/make-name-finder named-entity-time-path))

(defn read-conf []
  (json/parse-string (slurp resource-path) true))

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

(defn process-data [data]
  (->> data
    (flatten)
    (group-by identity)
    (map (fn [[k v]] [k (count v)]))
    (sort-by (fn [[k v]] v))
    (reverse)
    (take 10)
    ))

(defn -main [& args]
  (let [conf (read-conf)
        creds (make-creds conf)
        num-tweets (conf :num-tweets-to-fetch)
        screen-name (conf :screen-name)
        data (->> (retrieve-tweet-feed creds screen-name num-tweets)
                  (map :text)
                  (map sentencize))
        tokenized (map tokenize data)]

        (println "users: " (process-data (map extract-users data)))
        (println)
        (println "persons: " (process-data (map entity-person-find tokenized)))
        (println)
        (println "locations: " (process-data (map entity-location-find tokenized)))
        (println)
        (println "organizations: " (process-data (map entity-organization-find tokenized)))
        (println)
        (println "time: " (process-data (map entity-time-find tokenized)))))
