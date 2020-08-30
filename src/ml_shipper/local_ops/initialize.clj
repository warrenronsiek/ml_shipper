(ns ml-shipper.local-ops.initialize
  (:require [cognitect.aws.client.api :as aws]))


(defn create-bucket
  ([suffix]
   (let [s3 (aws/client {:api :s3})
         sts (aws/client {:api :sts})
         account (:Account (aws/invoke sts {:op :GetCallerIdentity}))]
     (aws/invoke s3 {:op :CreateBucket :request {:Bucket                    (str "ml-shipper-" suffix)
                                                 :ACL                       "private"
                                                 :CreateBucketConfiguration {:LocationConstraint "us-west-2"}}})))
  ([] (let [s3 (aws/client {:api :s3})
            sts (aws/client {:api :sts})
            account (:Account (aws/invoke sts {:op :GetCallerIdentity}))]
        (aws/invoke s3 {:op :CreateBucket :request {:Bucket                    (str "ml-shipper-" account)
                                                    :ACL                       "private"
                                                    :CreateBucketConfiguration {:LocationConstraint "us-west-2"}}}))))