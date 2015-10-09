(ns datascript-gc.core
  (:require [datascript.core :as d]))


(defn find-live-set
  "Given a DB, root eids, and reference attributes, return a
  set of all live eids"
  [db root-eids attrs]
  (loop [live (transient (set root-eids))]
    (let [new-live (reduce
                     (fn [live attr]
                       (reduce
                         (fn [live [e a v]]
                           (let [e-live (live e)
                                 v-live (live v)]
                             (cond
                               (and e-live v-live) live
                               e-live (conj! live v)
                               v-live (conj! live e)
                               :else live)))
                         live
                         (d/datoms db :aevt attr)))
                     live
                     attrs)]
      (if (identical? live new-live)
        (persistent! live)
        (recur new-live)))))


(defn garbage-collect
  "Given a Datascript DB, a set of root eids, and refrence attrs,
  return a list of retractions against all non referenced entities."
  [db root-eids attrs]
  (let [live-set (find-live-set db root-eids attrs)]
    (into []
          (keep (fn [[e a v]]
                  (when (not (live-set e))
                    [:db/retract e a v])))
          (d/datoms db :eavt))))


(defn ref-attrs
  "Given a Datascript Schema, return all the ref attributes"
  [schema]
  (set (for [[a opts] schema
             :when (= (:db/valueType opts) :db.type/ref)]
         a)))