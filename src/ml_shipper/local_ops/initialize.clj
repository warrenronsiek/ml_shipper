(ns ml-shipper.local-ops.initialize
  (:require [cognitect.aws.client.api :as aws]
            [clojure.java.shell :as shell]
            [me.raynes.fs :refer [copy-dir]]
            [clojure.java.io :as io]
            [clojure.string :as string]))


(defn get-create-bucket
  "returns the name of the bucket if it was created or already exists"
  []
  (let [s3 (aws/client {:api :s3})
        sts (aws/client {:api :sts})
        account (:Account (aws/invoke sts {:op :GetCallerIdentity}))
        bucket-name (str "ml-shipper-" account)
        response (aws/invoke s3 {:op :CreateBucket :request {:Bucket                    bucket-name
                                                             :ACL                       "private"
                                                             :CreateBucketConfiguration {:LocationConstraint "us-west-2"}}})]
    (cond
      (:Location response) bucket-name
      (= (:Code (:Error response)) "BucketAlreadyOwnedByYou") bucket-name
      :else (throw (Exception. "Unable to create bucket.")))))


(defn copy-terraform
  "copies terraform resources into state directory"
  [bucket-name]
  (let [terra-dir (str (System/getProperty "user.home") "/ml-shipper/terraform")
        provider (str terra-dir "/provider.tf")]
    (copy-dir (io/file (io/resource "terraform")) (io/file terra-dir))
    (spit provider (string/replace (slurp provider) #"BUCKET" bucket-name))))

(defn build-terraform
  "creates the terraform resources required to read from the bucket"
  []
  ())