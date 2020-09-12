(ns ml-shipper.local-ops.initialize
  (:require [cognitect.aws.client.api :as aws]
            [clojure.java.shell :as shell]
            [me.raynes.fs :refer [copy-dir]]
            [clojure.java.io :as io]
            [clojure.string :as string]))


(defn get-create-bucket
  "returns the name of the bucket if it was created or already exists, optional
  argument 'env' is a string"
  [& {:keys [env] :or {env false}}]
  (let [s3 (aws/client {:api :s3})
        sts (aws/client {:api :sts})
        account (:Account (aws/invoke sts {:op :GetCallerIdentity}))
        bucket-name (str "ml-shipper-" (if env (str env "-") "") account)
        response (aws/invoke s3 {:op :CreateBucket :request {:Bucket                    bucket-name
                                                             :ACL                       "private"
                                                             :CreateBucketConfiguration {:LocationConstraint "us-west-2"}}})]
    (cond
      (:Location response) bucket-name
      (= (:Code (:Error response)) "BucketAlreadyOwnedByYou") bucket-name
      :else (do
              (println response)
              (throw (Exception. "Unable to create bucket."))))))


(defn copy-terraform
  "copies terraform resources into state directory"
  [bucket-name & {:keys [env] :or {env false}}]
  (let [terra-dir (str (System/getProperty "user.home")
                       (if env (str "/ml-shipper-" env "/terraform") "/ml-shipper/terraform"))
        provider (str terra-dir "/provider.tf")]
    (io/make-parents terra-dir)
    (copy-dir (io/file (io/resource "terraform")) (io/file terra-dir))
    (spit provider (string/replace (slurp provider) #"BUCKET" (str "\"" bucket-name "\"")))
    terra-dir))

(defn build-terraform
  "creates the terraform resources required to read from the bucket"
  []
  ())