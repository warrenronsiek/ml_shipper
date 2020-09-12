(ns ml-shipper.local-ops.initialize-project-test
  (:require [clojure.test :refer :all]
            [ml-shipper.local-ops.initialize :refer :all]
            [ml-shipper.local-ops.utils :refer [tail-recursive-delete]]
            [cognitect.aws.client.api :as aws]
            [clojure.java.io :as io]))

(def s3 (aws/client {:api :s3}))
(def local-dir (str (System/getProperty "user.home") "/ml-shipper-test/terraform"))
(def ^:dynamic *bucket-name*)

(defn s3-setup-teardown [test-func]
  (let [sts (aws/client {:api :sts})
        bucket-name (str "ml-shipper-test-" (:Account (aws/invoke sts {:op :GetCallerIdentity})))]
    (copy-terraform (get-create-bucket :env "test") :env "test")
    (binding [*bucket-name* bucket-name] (test-func))
    (#(aws/invoke s3 {:op :DeleteBucket :request {:Bucket bucket-name}}))
    (tail-recursive-delete local-dir)))
(use-fixtures :once s3-setup-teardown)


(deftest create-bucket-test
  (testing "bucket exists"
    (is (= (reduce
             (fn [_ x] (if (= x *bucket-name*) (reduced x) nil))
             (map :Name (:Buckets (aws/invoke s3 {:op :ListBuckets}))))
           *bucket-name*))))

(deftest copy-terraform-test
  (testing "terraform gets copied"
    (is (.exists (io/file local-dir)))
    (is (.contains (slurp (io/file (str local-dir "/provider.tf")))
                   *bucket-name*))))