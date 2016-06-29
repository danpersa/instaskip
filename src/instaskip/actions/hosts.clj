(in-ns 'instaskip.actions)

(defn list-hosts [innkeeper-config]
  (let [ids-to-hosts (ik/get-hosts innkeeper-config)]
    (println "All hosts")
    (t/table ids-to-hosts)))
