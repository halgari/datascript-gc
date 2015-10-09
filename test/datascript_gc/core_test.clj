(ns datascript-gc.core-test
  (:require [clojure.test :refer :all]
            [datascript.core :as d]
            [datascript-gc.core :refer :all]))

;; Write a simple graph of data
;; a <- b -> c
;;        -> d
;; e -> f


(def init-tx [[:db/add 1 :value "a"]

              [:db/add 2 :value "b"]
              [:db/add 2 :parent 1]
              [:db/add 2 :child 3]
              [:db/add 2 :child 4]

              [:db/add 3 :value "c"]

              [:db/add 4 :value "d"]

              [:db/add 5 :value "e"]
              [:db/add 5 :child 6]

              [:db/add 6 :value "f"]])

(defn get-values [db]
  (set (map first (d/q '[:find ?value
                         :where
                         [?id :value ?value]]
                       db))))

(deftest basic-gc
  (let [db-conn (d/create-conn {:child {:db/cardinality :db.cardinality/many
                                        :db/valueType :db.type/ref}
                                :parent {:db/valueType :db.type/ref}})
        _ (d/transact! db-conn init-tx)
        db @db-conn]
    (is (= (get-values db)
           #{"a" "b" "c" "d" "e" "f"}))


    (is (= (set (garbage-collect db [1] #{:child :parent}))
           #{[:db/retract 5 :value "e"]
             [:db/retract 5 :child 6]

             [:db/retract 6 :value "f"]}))

    (is (= (set (garbage-collect db [5] #{:child :parent}))
           #{[:db/retract 1 :value "a"]

             [:db/retract 2 :value "b"]
             [:db/retract 2 :parent 1]
             [:db/retract 2 :child 3]
             [:db/retract 2 :child 4]

             [:db/retract 3 :value "c"]

             [:db/retract 4 :value "d"]}))

    (let [tx (garbage-collect db [5] (ref-attrs (:schema db)))]
      (d/transact! db-conn tx)
      (is (= (get-values @db-conn)
             #{"e" "f"})))))

(deftest ref-attrs-test
  (is (= (ref-attrs {:child {:db/cardinality :db.cardinality/many
                                 :db/valueType :db.type/ref}
                         :parent {:db/valueType :db.type/ref}
                         :something {:db/cardinality :db.cardinality/many}})

         #{:child :parent})))

