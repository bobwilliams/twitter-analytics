(ns twitter_analytics.util
  (:require [clojure.string :as str]))

(defn extract-users [text]
  (->> text
       (re-seq #"@(\w+)")
       (map (fn [[x y]] y))
       (map str/lower-case)))

(defn remove-spaces [s]
  (str/replace s #"\s+" ""))

(defn key-it [k]
  (keyword (remove-spaces k)))