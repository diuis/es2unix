(ns es.command.health
  (:require [es.data.cluster :as cluster]))

(def cols
  [['cluster  :cluster_name]
   ['status   :status]
   ['nodes    :number_of_nodes]
   ['data     :number_of_data_nodes]
   ['pri      :active_primary_shards]
   ['shards   :active_shards]
   ['relo     :relocating_shards]
   ['init     :initializing_shards]
   ['unassign :unassigned_shards]])

(defn health [http args {:keys [verbose]}]
  (concat
   (if verbose
     [(map (comp name first) cols)])
   (let [res (cluster/health http)
         vals (apply juxt (map second cols))]
     [(vals res)])))
