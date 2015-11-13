(ns twitter_analytics.models
  (:require [clojure.java.io :as io]
            [opennlp.nlp :as nlp]))

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
