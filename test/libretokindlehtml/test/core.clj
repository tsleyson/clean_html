(ns libretokindlehtml.test.core
  (:use [libretokindlehtml.core])
  (:use [clojure.test]))

(deftest test-order-map
  (do
    (is (= (order-map :notorder 'something) 'something))
    (is (= (order-map :order ["pro" "c1" "c2" "c3" "after"])
        {"pro" 0, "c1" 1, "c2" 2, "c3" 3, "after" 4}))))

(deftest test-list-of-resources
  (let [correct-str (slurp "test-resources/listofres.txt")
        result-str (pr-str (list-of-resources "resources/testdata"))]
    (is (= result-str correct-str))))
