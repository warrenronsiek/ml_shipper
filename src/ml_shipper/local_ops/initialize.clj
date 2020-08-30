(ns ml-shipper.local-ops.initialize
  (:require [cognitect.aws.client.api :as aws]))


(defn create-bucket
  "returns a boolean value to indicate if the bucket is created or not"
  ([& suffix]
   (let [s3 (aws/client {:api :s3})
         sts (aws/client {:api :sts})
         account (:Account (aws/invoke sts {:op :GetCallerIdentity}))
         bucket-name (str "ml-shipper-" (or (first suffix) account))
         response (aws/invoke s3 {:op :CreateBucket :request {:Bucket                    bucket-name
                                                              :ACL                       "private"
                                                              :CreateBucketConfiguration {:LocationConstraint "us-west-2"}}})]
     (if (:Location response) bucket-name false))))