(ns libretokindlehtml.test.config-reader
  (:require [libretokindlehtml.config-reader :refer :all]
            [libretokindlehtml.test.core :refer [config-map]]
            [clojure.test :refer :all]))
; The fixture might have stuff we don't use in every test,
; but running tests isn't part of the program so it's okay
; if it takes a minute.
(defn setup
  [test] 
  (do (def valid {:directory "somewhere",
                  :order ["f1", "f2", "f3"],
                  :title "The title",
                  :subtitle "The subtitle",
                  :authors ["a1", "a2"],
                  :template "path/to/template",
                  :stylesheet "path/to/stylesheet"}) 
      (def invalid {:directory "somewhere",
                    :titlo  "The titlo",
                    :subtitle "The subtitle",
                    :authors ["a1", "a2"],
                    :stylesheet "path/to/stylesheet"}) 
      (def ofnightcf (read-config-file "resources/ofnight/config.json"))
      (def transcf (read-config-file "test-resources/testhtml/config.json"))
      (test)))

(use-fixtures :each setup)

(deftest test-order-map
  (testing "Returns intact when key not :order" 
    (is (= (order-map :notorder 'something) 'something))
    (is (= (order-map :merge true) true)))
  (testing "Correctly assigns order positions when key is :order." 
    (is (= (order-map :order ["pro" "c1" "c2" "c3" "after"])
           {"pro" 0, "c1" 1, "c2" 2, "c3" 3, "after" 4}))))

(deftest test-read-config
  (testing "Reads map out of file."
    (is (= (config-map) (read-config-file "test-resources/testconfig.json")))))

(deftest test-validate-config
  (testing "A valid, dummy config map"
    (is (= valid (validate-config valid))))
  (testing "A valid real-world config map."
    (is (= ofnightcf (validate-config ofnightcf))))
  (testing "A second valid real-world config map."
    (is (= transcf (validate-config transcf))))
  (testing "An invalid config map" 
    (try (validate-config invalid)
         (catch clojure.lang.ExceptionInfo e
          (let [exinf (ex-data e)]
            (is (= (:type exinf) "Bad config info"))
            (is (= (:cause exinf) "Invalid key in map.\nMissing required keys #{:order}.")))))))

(deftest test-read-config
  (testing "Of Night's config file (valid)"
    (is (= ofnightcf (read-config "resources/ofnight/config.json"))))
  (testing "Translation's config file (valid)"
    (is (= transcf (read-config "test-resources/testhtml/config.json"))))
  (testing "Bad config file (should fail)"
    (try (read-config "test-resources/badconfig.json")
         (catch clojure.lang.ExceptionInfo e
           (let [exinf (ex-data e)]
             (is (= (:type exinf) "Bad config info"))
             (is (= (:cause exinf) "Invalid key in map.\nMissing required keys #{:order}.")))))))
; Note that in general you can test for exceptions like this:
; (is (thrown? ExceptionClass (form that should throw exception)))
; and you can check its message like this:
; (is (thrown-with-msg? ExceptionClass #"Regex that matches 
; the message you're expecting" (form that throws exception)))
