(defproject ml_shipper_cli "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :main ml-shipper-cli.core
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [cli-matic "0.4.3"]
                 [me.raynes/fs "1.4.6"]
                 [clj-python/libpython-clj "1.45"]]
  :repl-options {:init-ns ml-shipper-cli.core}
  :profiles {:uberjar {:aot :all}})
