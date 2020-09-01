(ns ml-shipper.local-ops.initialize
  (:require [cognitect.aws.client.api :as aws]))


(defn create-bucket
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