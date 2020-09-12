(ns ml-shipper.local-ops.utils
  (:require [clojure.java.io :as io]))

(defn tail-recursive-delete
  [& fs]
  (when-let [f (first fs)]
    (if-let [cs (seq (.listFiles (io/file f)))]
      (recur (concat cs fs))
      (do (io/delete-file f)
          (recur (rest fs))))))
