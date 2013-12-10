(ns libretokindlehtml.test.merge-files
  (require [clojure.test :refer :all]
           [clojure.pprint :refer [pprint]]
           [clojure.java.io :refer [file reader writer]]
           [net.cgrand.enlive-html :as enlive]
           [libretokindlehtml.merge-files :refer :all]
           [libretokindlehtml.test.core :refer [config-map check-meta]]))
; I could rewrite a lot of the lets with a fixture but I won't.

(deftest test-list-of-resources
  (let [correct-str (slurp "test-resources/listofres.txt")
        result (list-of-resources (config-map))
        fromstr (list-of-resources "test-resources/testconfig.json")]
    (do
      (testing "Listing from a config map" 
        (is (= (pr-str result) correct-str)))
      (testing "Listing from a string" 
        (is (= (pr-str fromstr) correct-str)))
      (testing "Whether the metadata is set correctly" 
        (let [metas [{:name "ClojureDocs - clojure.core_atom.html",
                      :position 0}
                     {:name "ClojureDocs - clojure.core_for.html",
                      :position 1}
                     {:name "ClojureDocs - clojure.pprint.html",
                      :position 2}]]
         (is (every? identity (map check-meta result metas))))))))

(deftest test-mine-content
  (let [correct (slurp "test-resources/test-mine-content.txt")
        output "test-resources/testhtml/Coolish Walk.html"]
    (testing "Mining from a string"
      (is (= (with-out-str (pprint (mine-content output))) correct))) 
    (testing "Mining from a java.io.File"
      (is (= (-> output (file) (mine-content) (pprint) (with-out-str)) correct))) 
    (testing "Mining from an Enlive html resource"
      (is (= (-> output (file) (enlive/html-resource) (mine-content) (pprint) (with-out-str)))))))

; If list-of-resources and mine-content both work, mine-all shouldn't
; have any problems, so there is no test for it.
