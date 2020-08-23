(ns ml-shipper-cli.local-ops.make-project
  (:require [libpython-clj.require :refer [require-python]]
            [libpython-clj.python :refer [py. py.. py.-] :as py]
            [clojure.java.io :as io]
            [ml-shipper-cli.vars :refer [version]])
  (:import (java.io File)))

(py/initialize! :python-executable "/usr/local/bin/python3.8")
(require-python 'venv)

(defn make-project [{:keys [name]}]
  (let [dir (str (System/getProperty "user.dir") "/./" name)]
    (println (str "creating virtualenv for project: " name))
    (.mkdir (File. dir))
    (py. (venv/EnvBuilder {:with_pip "True"}) create (str dir "/venv"))
    (println (str "creating project requirements"))
    (spit (str dir "/requirements.txt") (str "ml_shipper==" version))
    (println "Done!")))

