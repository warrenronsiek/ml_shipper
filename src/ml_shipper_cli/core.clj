(ns ml-shipper-cli.core
  (:require [cli-matic.core :refer [run-cmd]])
  (:require [ml-shipper-cli.local-ops.make-project :refer [make-project]])
  (:gen-class))

(defn hello [{:keys [name]}] (println (str "Hi! " name)))

(def CONFIGURATION
  {:app {:command "mlship"
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
              ]})

(defn -main [& args] (run-cmd args CONFIGURATION))