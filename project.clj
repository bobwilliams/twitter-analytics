(defproject twitter-analytics "0.1.0-SNAPSHOT"
  :description "Twitter stats and NLP"
  :url "http://bob.codes"
  :main twitter_analytics.core
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [cheshire "5.5.0"]
                 [twitter-api "0.7.8"]
                 [clojure-opennlp "0.3.3"]])
