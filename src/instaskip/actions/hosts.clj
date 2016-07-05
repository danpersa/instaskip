(in-ns 'instaskip.actions)

(defn list-hosts [innkeeper-config]
  (let [ids-to-hosts-try (exc/try-on (ik/get-hosts innkeeper-config))]

    (m/matchm ids-to-hosts-try
              {:success ids-to-hosts}
              (do (cl/info "All hosts")
                  (t/table ids-to-hosts))
              {:failure ex}
              (cl/error ex))))
