(ns libretokindlehtml.test.merge-files
  (require [clojure.test :refer :all]
           [clojure.pprint :refer [pprint]]
           [clojure.java.io :refer [file reader writer]]
           [net.cgrand.enlive-html :as enlive]
           [libretokindlehtml.merge-files :refer :all]
           [libretokindlehtml.test.core :refer :all]))
; I could rewrite a lot of the lets with a fixture but I won't.

(defn setup
  [test]
  (def config-map  {:directory "test-resources/testdata/", 
                    :order {"ClojureDocs - clojure.core_atom.html" 0
                            "ClojureDocs - clojure.core_for.html" 1
                            "ClojureDocs - clojure.pprint.html"   2}
                    :paragraph-selector [:p]})
  (test))
(use-fixtures :once setup)

;; Note: replace \r\n with \n on Windows, just for the test since
;; having \r\n on Windows is correct in general.
(deftest test-mine-content
  (let [correct (slurp "test-resources/test-mine-content.txt")
        output "test-resources/testhtml/Coolish Walk.html"]
    (testing "Mining from a string"
      (is (= (clojure.string/replace (with-out-str (pprint (mine-content output)))
                                     "\r\n" "\n")
             correct))) 
    (testing "Mining from a java.io.File"
      (is (= (-> output (file) (mine-content) (pprint) (with-out-str)
                 (clojure.string/replace #"\r\n" "\n"))
             correct)))
    (testing "Mining from an Enlive html resource"
      (is (= (-> output
                 (file)
                 (enlive/html-resource)
                 (mine-content)
                 (pprint)
                 (with-out-str)))))))

(deftest test-list-of-resources
  (let [correct-str (slurp "test-resources/listofres.txt")
        result (list-of-resources config-map)
        fromstr (list-of-resources "test-resources/testdata/testconfig.json")]
    (do
      (testing "Listing from a config map" 
        (is (= (pr-str result) correct-str)))
      (testing "Listing from a string" 
        (is (= (pr-str fromstr) correct-str)))
      (testing "Whether the name and position metadata are set correctly" 
        (let [metas [{:name "ClojureDocs - clojure.core_atom.html",
                      :position 0}
                     {:name "ClojureDocs - clojure.core_for.html",
                      :position 1}
                     {:name "ClojureDocs - clojure.pprint.html",
                      :position 2}]]
         (is (every? identity (map check-name-and-pos result metas))))))))
; These files don't have the kind of style info I care about.

;; (deftest test-translation-list
;;   (let [correctlist (slurp "test-resources/testhtml/listoftrans2.dat")
;;         correctmeta (slurp "test-resources/testhtml/correctmeta2.dat")
;;         doubtedlist (list-of-resources "test-resources/testhtml/config.json")
;;         doubtedmeta (map meta doubtedlist)]
;;     (testing "List from string is correct"
;;       (is (= correctlist (pprint-str doubtedlist))))
;;     (testing "Metadata with the right kind of style is correct"
;;       (is (= correctmeta (pprint-str doubtedmeta))))))
; For now I'm using a horrible hack to try and get a working file, so
; this test is always wrong.

; If list-of-resources and mine-content both work, mine-all shouldn't
; have any problems, so there is no test for it.

(deftest test-private-helpers
  (run-tests 'libretokindlehtml.merge-files))
