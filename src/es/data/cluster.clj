(ns es.data.cluster
  (:refer-clojure :exclude [count])
  (:require [es.data.replica :as replica]
            [es.data.nodes :as nodes]
            [es.format.uri :as uri]
            [es.util :as util]))

(defn health
  ([http]
     (http "/_cluster/health"))
  ([http indices]
     (let [healths (http "/_cluster/health?level=indices")]
       (->> (for [[nam data] (:indices healths)]
              (if (util/match-any? (name nam) indices)
                [nam data]))
            (filter identity)
            (into {})))))

(defn state [http]
  (http "/_cluster/state?filter_metadata=1"))

(defn unassigned-shards
  ([http]
     (unassigned-shards http []))
  ([http indices]
     (let [st (state http)]
       (filter identity
               (for [replica (-> st :routing_nodes :unassigned)]
                 (replica/maybe replica indices))))))

(defn shards
  ([http]
     (shards http []))
  ([http indices]
     (let [st (state http)]
       (filter identity
               (for [[idxname index] (-> st :routing_table :indices)
                     [shname shard] (:shards index)
                     replica shard]
                 (replica/maybe replica indices))))))

(defn count
  ([http]
     (count http "*:*"))
  ([http query]
     (http (str "/_count?q=" (uri/encode query)))))

(defn flaggable-nodes [http path flags]
  (let [path (str path "?" (uri/query-flags flags))]
    (http path)))

(defn stats [http & flags]
  (flaggable-nodes http "/_nodes/stats" flags))

(defn info [http & flags]
  (flaggable-nodes http "/_nodes" flags))

(defn nodes [http & flags]
  (util/merge-transpose
   {:stats (:nodes (apply stats http flags))}
   {:info (:nodes (apply info http flags))}))

(defn mem [http]
  (->> (for [[id node] (nodes http :jvm)]
         (let [stat (get-in node [:stats :jvm :mem])
               info (get-in node [:info :jvm :mem])]
           [id (merge
                (nodes/mem stat info)
                (select-keys (:info node) [:name :transport_address]))]))
       (into {})))
