(defproject tuck-remoting-example "0.1-SNAPSHOT"
  :dependencies ~(into [['reagent "0.8.1"]
                        ['figwheel "0.5.16"]
                        ['data-frisk-reagent "0.4.5"]]
                       (mapv (fn [[dep {ver :mvn/version}]]
                               [dep ver])
                             (read-string (slurp "../deps.edn"))))
  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-figwheel "0.5.16"]]

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs" "src/cljc" "../src"]
                        :figwheel {:on-jsload "chat.main/reload-hook"}
                        :compiler {:optimizations :none
                                   :output-to "resources/public/js/compiled/chat.js"
                                   :output-dir "resources/public/js/compiled/out"}}]}
  :main chat.server
  :source-paths ["src/clj" "src/cljc" "../src"])
