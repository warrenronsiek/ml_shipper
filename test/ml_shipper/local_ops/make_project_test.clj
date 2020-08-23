(ns ml-shipper.local-ops.make-project-test
  (:require [clojure.test :refer :all]
            [ml-shipper-cli.local-ops.make-project :refer :all]
            [clojure.java.io :as io]
            [ml-shipper-cli.vars :refer [version]]
            [clj-yaml.core :as yaml]))

(def ^:dynamic *proj-name*)

(defn tail-recursive-delete
  [& fs]
  (when-let [f (first fs)]
    (if-let [cs (seq (.listFiles (io/file f)))]
      (recur (concat cs fs))
      (do (io/delete-file f)
          (recur (rest fs))))))

(defn venv-setup-teardown [test-func]
  (let [venv-name (gensym "proj")]
    (make-project {:name venv-name})
    (binding [*proj-name* venv-name] (test-func))
    (tail-recursive-delete (str venv-name))))
(use-fixtures :once venv-setup-teardown)

(deftest project-creation
  (testing "project creation"
    (is (.exists (io/file (str *proj-name* "/venv"))))
    (is (.exists (io/file (str *proj-name* "/requirements.txt"))))
    (is (= (slurp (str *proj-name* "/requirements.txt")) (str "ml_shipper==" version)))
    (is (.exists (io/file (str *proj-name* "/ml_shipper.yml"))))
    (is (= (-> (yaml/parse-string (slurp (str *proj-name* "/ml_shipper.yml")))
               (:ml-shipper) (:name))
           (str *proj-name*)))))