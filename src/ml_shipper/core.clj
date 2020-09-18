(ns ml-shipper.core
  (:require [cli-matic.core :refer [run-cmd]])
  (:require [ml-shipper.local-ops.make-project :refer []])
  (:gen-class :main true))

(defn hello [{:keys [name]}] (println (str "Hi! " name)))


(defn -main [& args]
  (require 'clojure.set)
  (binding [*ns* *ns*]
    ;;rather than :require it in the ns-decl, we load it
    ;;at runtime.
    (require 'ml-shipper.local-ops.make-project)
    (in-ns 'ml-shipper.local-ops.make-project)
    ;;if we don't use resolve, then we get compile-time aot
    ;;dependency on marathon.core.  This allows us to shim the
    ;;class.
    ((resolve 'ml-shipper.local-ops.make-project/make-project))
    (let [CONFIGURATION {:app {:command "mlship"
                               :description "ships your ml!"
                               :version "0.0.1"}
                         :commands [{:command "hi"
                                     :short "h"
                                     :description ["says hi"]
                                     :opts [{:option "name" :short "n" :type :string :default ""}]
                                     :runs hello}
                                    {:command "mkproj"
                                     :short "mk"
                                     :description ["creates project scaffolding"]
                                     :opts [{:option "name" :short "n" :type :string :default "ml_shipper_example"}]
                                     :runs make-project}
                                    ;{:command "configure"
                                    ; :short "conf"
                                    ; :description ["configures the ml-shipper cli"]
                                    ; :opts [{:option "backend-id" :short "b" :type :string}]}
                                    ]}]
      (run-cmd args CONFIGURATION))
    )

  )
