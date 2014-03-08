(ns libretokindlehtml.test.config-reader
  (:require [libretokindlehtml.config-reader :refer :all]
            [clojure.test :refer :all]
            [net.cgrand.enlive-html :refer :all]))
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
                  :heading-selector [:h1],
                  :paragraph-selector [:p],
                  :cleaner identity})
      (def invalid {:directory "somewhere",
                    :titlo  "The titlo",
                    :subtitle "The subtitle",
                    :authors ["a1", "a2"],
                    :stylesheet "path/to/stylesheet"})
      (def config-map   {:directory "test-resources/testdata/", 
                         :order {"ClojureDocs - clojure.core_atom.html" 0
                                 "ClojureDocs - clojure.core_for.html" 1
                                 "ClojureDocs - clojure.pprint.html"   2}
                         :paragraph-selector [:p]})
      (def config-map-order-vector {:directory "test-resources/testdata/", 
                                    :order [ "ClojureDocs - clojure.core_atom.html"
                                             "ClojureDocs - clojure.core_for.html"
                                             "ClojureDocs - clojure.pprint.html" ]
                                    :paragraph-selector [:p]})
      (def ofnightcf (read-config "resources/ofnight/ofnightconfig.clj"))
      (def transcf (read-config "test-resources/testhtml/config.clj"))
      (test)))

(use-fixtures :once setup)
;; You don't need fixtures for stuff that doesn't change. None of this
;; stuff actually changes in a test because of Clojure's purely
;; functional data structures, so a :once fixture or just top-level
;; def is fine.

;; Note: none of these tests cover config files that have cleaner
;; functions. The reason is that the cleaner function is assigned an
;; internal ID number every time it's read it, so it's not possible to
;; compare them. I don't think it matters, but there's probably some
;; way to make it compare the doc strings to be sure they're the same function.

(deftest test-read-config-file
  (testing "Reads map out of file."
    (is (= config-map-order-vector
           (read-config-file "test-resources/testdata/testconfig.clj"))))
  (testing "Has order vector."
    (is (= (type (:order (read-config-file "test-resources/testdata/testconfig.clj")))
           clojure.lang.PersistentVector))))

(deftest test-order-map
  (testing "Correctly assigns order positions when key is :order." 
    (is (= (order-map {:order ["pro" "c1" "c2" "c3" "after"]})
           {:order {"pro" 0, "c1" 1, "c2" 2, "c3" 3, "after" 4}})))
  (testing "Leaves alone other keys in map."
    (is (= (order-map {:order ["c1", "c2", "c3"], :not-order 'this})
           {:order {"c1" 0, "c2" 1, "c3" 2}, :not-order 'this}))))

(deftest test-validate-config
  (testing "A valid, dummy config map."
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
            (is (= (:cause exinf)
                   "Invalid key in map.\nMissing required keys #{:paragraph-selector :order}."))))))
  (testing "Another invalid config map"
    (try (validate-config (read-config-file "test-resources/badconfig.clj"))
         (catch clojure.lang.ExceptionInfo ei
           (let [exinf (ex-data ei)]
             (is (= (:type exinf) "Bad config info"))
             (is (= (:cause exinf)
                    "Invalid key in map.\nMissing required keys #{:paragraph-selector :order}.")))))))

(deftest test-read-config
  (testing "Of Night's config file (valid)"
    (is (= ofnightcf (read-config "resources/ofnight/ofnightconfig.clj"))))
  (testing "Translation's config file (valid)"
    (is (= transcf (read-config "test-resources/testhtml/config.clj"))))
  (testing "Has an order map instead of an order vector"
    (is (associative? (:order transcf)))
    (is (associative? (:order ofnightcf))))
  (testing "Bad config file (should fail)"
    (try (read-config "test-resources/badconfig.clj")
         (catch clojure.lang.ExceptionInfo e
           (let [exinf (ex-data e)]
             (is (= (:type exinf) "Bad config info"))
             (is (= (:cause exinf) "Invalid key in map.\nMissing required keys #{:paragraph-selector}.")))))))
; Note that in general you can test for exceptions like this:
; (is (thrown? ExceptionClass (form that should throw exception)))
; and you can check its message like this:
; (is (thrown-with-msg? ExceptionClass #"Regex that matches 
; the message you're expecting" (form that throws exception)))


;; (testing "Cleaner function gets evaluated properly"
;;     (let [strawsuncf (read-config "resources/strawberrysunflower/config.clj")]
;;       (is (ifn? (:cleaner strawsuncf)))
;;       (is (= ["{:tag :p :content [\"the content—that we wanted\"]}"]
;;              ((:cleaner strawsuncf)
;;               "{:tag :p :content [\"the content--that we wanted\"]}")))))
