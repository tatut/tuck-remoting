(defproject tuck-remoting "20180916-alpha"
  :dependencies ~(mapv (fn [[dep {ver :mvn/version}]]
                         [dep ver])
                       (read-string (slurp "deps.edn"))))
