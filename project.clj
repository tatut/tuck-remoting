(defproject webjure/tuck-remoting "20190213"
  :dependencies ~(mapv (fn [[dep {ver :mvn/version}]]
                         [dep ver])
                       (read-string (slurp "deps.edn"))))
