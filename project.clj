(defproject tuck-remoting "20181119-alpha"
  :dependencies ~(mapv (fn [[dep {ver :mvn/version}]]
                         [dep ver])
                       (read-string (slurp "deps.edn"))))
