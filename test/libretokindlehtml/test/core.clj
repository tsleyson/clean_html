(ns libretokindlehtml.test.core
  (:require [libretokindlehtml.core :refer :all]
            [libretokindlehtml.merge-files :refer :all]
            [libretokindlehtml.config-reader :refer :all]
            [libretokindlehtml.libreoffice :refer :all]
            [clojure.test :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :refer :all]
            [net.cgrand.enlive-html :as enlive]))
; It's probably fine now, but in future you might want to make separate testing
; files and namespaces for each file. Then you can use fixtures. With this I can't
; use them because the data for each test is completely different.

; You also might want to consider writing a macro for this common pattern
; of setting up stuff in a let and then using a do form with a bunch of
; ises in it (yay, finally a use for macros!!)

; Helper functions for the tests.
(defn config-map
  "The fixture facility doesn't do exactly what I want so 
   this is my primitive workaround. Returns fake config data."
  []
  {:directory "resources/testdata/", 
   :order {"ClojureDocs - clojure.core_atom.html" 0
            "ClojureDocs - clojure.core_for.html" 1
            "ClojureDocs - clojure.pprint.html"   2} 
   :merge true})

(defn check-meta
  "Checks whether a metadata map is correct."
  [obj metamap]
  (do
    ;(pprint (meta obj))
    (= (meta obj) metamap)))

; The tests! Starting with config-reader.
(deftest test-order-map
  (do
    (is (= (order-map :notorder 'something) 'something))
    (is (= (order-map :merge true) true))
    (is (= (order-map :order ["pro" "c1" "c2" "c3" "after"])
        {"pro" 0, "c1" 1, "c2" 2, "c3" 3, "after" 4}))))

(deftest test-read-config
  (is (= (config-map) (read-config "test-resources/testconfig.json"))))

; merge-files.
(deftest test-list-of-resources
  (let [correct-str (slurp "test-resources/listofres.txt")
        result  (list-of-resources (config-map))
        fromstr (list-of-resources "test-resources/testconfig.json")]
    (do
      (is (= (pr-str result) correct-str))
      (is (= (pr-str fromstr) correct-str))
      (let [metas [{:name "ClojureDocs - clojure.core_atom.html", 
                    :position 0}
                   {:name "ClojureDocs - clojure.core_for.html", 
                    :position 1}
                   {:name "ClojureDocs - clojure.pprint.html", 
                    :position 2}]]
        (is (every? identity (map check-meta result metas)))))))

(deftest test-mine-content
  (let [correct (slurp "test-resources/test-mine-content.txt")
        output  "test-resources/testhtml/Coolish Walk.html"]    
    (do 
      (is (= (with-out-str (pprint (mine-content output))) correct))
      (is (= (-> output (file) (mine-content) (pprint) (with-out-str)) correct))
      (is (= (-> output (file) (enlive/html-resource) (mine-content) (pprint) (with-out-str)))))))

(deftest test-merge-resources
  (let [correct (slurp "test-resources/test-merge-resources.txt")
        result  (merge-resources (list-of-resources "test-resources/testhtml/config.json"))]
    (do
      (is (= (with-out-str (pprint result)) correct))
      (is (= 3 (count (enlive/select result [#{:html :head :body}]))))
       (comment "Should only have one of each of those tags"))))

; It was too soon to write a test for this, especially for the cleaning
; version. It might never be the right time to unit test this since
; the output is so complex and subjective.

;; (deftest test-chapter
;;   (let [dirty-correct (slurp "test-resources/test-chapter.txt")
;;         ;clean-correct (slurp "test-resources/test-chapter-2.txt")
;;         page1 (mine-content "resources/ofnight/Chapter 3.html")
;;         ;mebae-correct (slurp "test-resources/test-chapter-3.txt")
;;         ;mebae-page (mine-content "test-resources/testhtml/Mebae Drive.html")
;;         ]
;;     (do
;;       ; test without giving a cleaner function to the snippet.
;;       (is (= (with-out-str (pprint (chapter page1))) dirty-correct))
;;       ;(is (= (with-out-str (pprint (chapter page1 (libre-maid)))) clean-correct))
;;       ;(is (= (with-out-str (pprint (chapter mebae-page (libre-maid)))) mebae-correct))
;;       )))
