(defproject webjure/tuck-remoting "20190213-SNAPSHOT"
  :dependencies ~(mapv (fn [[dep {ver :mvn/version}]]
                         [dep ver])
                       (read-string (slurp "deps.edn"))))
