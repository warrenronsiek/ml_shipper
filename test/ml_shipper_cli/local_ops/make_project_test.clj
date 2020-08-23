(ns ml-shipper-cli.local-ops.make-project-test
  (:require [clojure.test :refer :all]
            [ml-shipper-cli.local-ops.make-project :refer :all]
            [clojure.java.io :as io]
            [clojure.java.shell :only [sh]]))

(def ^:dynamic *venv-name*)

; for some reason this cleanup method throws an error when run in tests, but otherwise
; works
;(defn tail-recursive-delete
;  [& fs]
;  (when-let [f (first fs)]
;    (if-let [cs (seq (.listFiles (io/file f)))]
;      (recur (concat cs fs))
;      (do (io/delete-file f)
;          (recur (rest fs))))))
; using shell delete instead.
(defn shell-delete [dir]
  (sh/sh "rm" "-rf" dir ))

(defn venv-setup-teardown [test-func]
  (let [venv-name (gensym "proj")]
    (make-project {:name venv-name})
    (binding [*venv-name* venv-name] (test-func))
    (shell-delete venv-name)))
(use-fixtures :once venv-setup-teardown)

(deftest venv-creation
  (testing "virtualenv creation"
    (is (.exists (io/file (str *venv-name* "/venv"))))))

(run-tests)