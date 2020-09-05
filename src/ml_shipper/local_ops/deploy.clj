(ns ml-shipper.local-ops.deploy
  (:require [cognitect.aws.client.api :as aws]
            [clojure.java.shell :as shell]
            [clojure.java.io :as io]
            [clojure.string :as str-ops]
            [ml-shipper.local-ops.initialize :refer [get-create-bucket]])
  (:import (java.security MessageDigest)
           (java.math BigInteger)))

(defn ^:private md5
  [^String s]
  (->> s
       .getBytes
       (.digest (MessageDigest/getInstance "MD5"))
       (BigInteger. 1)
       (format "%032x")))

(defn package "zips up the code into a package, returns name of package" []
  (let [tmp-file-name "/tmp/out.zip"]
    (shell/sh "zip" "-r" tmp-file-name ".")
    (let [md5-file-name (str "/tmp/" (md5 (slurp tmp-file-name)) ".zip")]
      (io/copy (io/file tmp-file-name) (io/file md5-file-name))
      (io/delete-file tmp-file-name)
      md5-file-name)))

(defn deploy "deploys a local project, returns key of deployed package" []
  (let [s3 (aws/client {:api :s3})
        package-name (package)
        key (str "deploy/" (last (str-ops/split package-name #"/")))]
    (aws/invoke s3 {:op :PutObject :request {:Bucket (get-create-bucket)
                                             :Key    key
                                             :Body   (.getBytes (slurp package-name))}})
    key))

