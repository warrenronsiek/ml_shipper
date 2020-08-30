(ns ml-shipper.local-ops.initialize-project-test
  (:require [clojure.test :refer :all]
            [ml-shipper.local-ops.initialize :refer :all]
            [cognitect.aws.client.api :as aws]))

(def s3 (aws/client {:api :s3}))
(def ^:dynamic *bucket-name*)

(defn s3-setup-teardown [test-func]
  (let [rand-name (subs (str (gensym)) 3)
        bucket-name (str "ml-shipper-" rand-name)]
    (create-bucket rand-name)
    (binding [*bucket-name* bucket-name] (test-func))
    (#(aws/invoke s3 {:op :DeleteBucket :request {:Bucket bucket-name}}))))
(use-fixtures :once s3-setup-teardown)


(deftest create-bucket-test
  (testing "bucket exists"
    (is (= (reduce
             (fn [_ x] (if (= x *bucket-name*) (reduced x) nil))
             (map :Name (:Buckets (aws/invoke s3 {:op :ListBuckets}))))
           *bucket-name*))))