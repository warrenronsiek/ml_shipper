(ns ml-shipper.local-ops.deploy-test
  (:require [clojure.test :refer :all]
            [ml-shipper.local-ops.deploy :refer :all]
            [ml-shipper.local-ops.initialize :refer [get-create-bucket]]
            [cognitect.aws.client.api :as aws]
            [clojure.java.io :as io]
            [clojure.string :as str-ops]))

(def s3 (aws/client {:api :s3}))

(deftest deploy-package-test
  (testing "package creation"
    (let [package-name (package)]
      (is (.exists (io/file package-name)))))
  (testing "package deployment"
    (let [package-name (package)
          deploy-key (deploy)]
      (is (= deploy-key (str "deploy/" (last (str-ops/split package-name #"/")))))
      (is (contains? (set (map :Key (:Contents (aws/invoke s3 {:op :ListObjectsV2 :request {:Bucket (get-create-bucket)
                                                                                            :Prefix "deploy/"}}))))
                     deploy-key)))))