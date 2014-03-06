(ns libretokindlehtml.test.novel
  "Some functions in novel aren't private (although they probably should have been)
and call for really complex tests, so they get their own file and don't have to
share template-helpers."
  (:require [libretokindlehtml.novel :refer :all]
            [net.cgrand.enlive-html :refer :all]
            [clojure.test :refer [testing deftest use-fixtures is are]]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as s]))

(defn result-string
  "Pretty prints result of a function call to a string 
and replaces Windows line endings with Unix"
  [function & args]
  (-> (apply function args)
      (pprint)
      (with-out-str)
      (s/replace #"\r\n" "\n")))

;; Test data
;; For make-paragraphs
(def baremin-res (html-resource (clojure.java.io/file "resources/templates/chaptersnip.html")))
(def correct-simple (result-string transform
                                   baremin-res
                                   [:.chapter :.chaptertext :p.standard] 
                                   (content "HELLO!!!!!!!!!")))
;; For insert-heading
(def head-select [[:h2 (attr= :class "heading")]])
;; Note: THE METADATA NEEDS TO BE ON THE SKELETONS!!!!
(def simple-heading [{:tag :h2, :attrs {:class "heading"}
                      :content [{:tag :a,
                                 :attrs {:id "name"},
                                 :content ["heading text"]}]}])
(def not-so-simple-heading [{:tag :span, :attrs {:class "pointless"},
                             :content []}
                            {:tag :p, :attrs {:class "standard"}
                             :content ["Some stuff that comes before the heading"]}
                            {:tag :h2, :attrs {:class "heading"}
                             :content [{:tag :a,
                                        :attrs {:id "name"}
                                        :content ["heading text"]}]}
                            {:tag :p, :attrs (:class "standard"),
                             :content ["Some stuff that comes after the heading"]}])
(def invalid-heading (-> [{:tag :h2, :attrs {:class "heading"}}
                          {:tag :h2, :attrs {:class "heading"}}]
                         (with-meta {:name "invalid"})))
(def without-heading (-> [{:tag :p, :attrs {:class "standard"}, :content ["The first paragraph"]},
                          {:tag :p, :attrs {:class "also_standard"}, :content ["The second paragraph"]}]
                         (with-meta {:name "not-here"})))
;; insert-heading should transform the skeletons into the headings.
(def simple-skeleton (-> [{:tag :h2, :attrs {:class "heading"}
                           :content ["heading text"]}]
                         (with-meta {:name "name"})))
(def not-so-simple-skeleton (-> [{:tag :span, :attrs {:class "pointless"},
                                  :content []}
                                 {:tag :p, :attrs {:class "standard"}
                                  :content ["Some stuff that comes before the heading"]}
                                 {:tag :h2, :attrs {:class "heading"}
                                  :content ["heading text"]}
                                 {:tag :p, :attrs (:class "standard"),
                                  :content ["Some stuff that comes after the heading"]}]
                                (with-meta {:name "name"})))
;; For chapter (and others)
(def test-config {:cleaner #(clojure.string/replace #":" "--")
                  :title "Simple Stuff"
                  :subtitle "The First Test"
                  :heading-selector [[:p (attr= :class "heading")] text-node]
                  :paragraph-selector [[:p (attr= :class "standard")]]
                  :directory "test-resources/snippettests"
                  :order {"ch1.html": 0, "ch2.html": 1, "ch3.html": 2}})
;; The test data for this is pretty complicated, so no with-test.
(deftest test-insert-heading
  (testing "Normal operation, simple and complex cases"
    (are [transformed written] (= transformed written)
         (transform simple-skeleton
                    head-select
                    (insert-heading simple-skeleton [head-select text-node]))
         simple-heading
         (transform not-so-simple-skeleton
                    head-select
                    (insert-heading not-so-simple-skeleton [head-select text-node]))
         not-so-simple-heading))
  (testing "Invalid map with non-unique heading selector"
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"selector yields non-unique answer"
                          (insert-heading invalid-heading head-select))))
  (testing "Chapter has no heading; should leave unchanged."
    (is (= without-heading
           (transform without-heading head-select (insert-heading without-heading [head-select text-node]))))
    (is (= simple-skeleton
           (transform simple-skeleton [:h3] (insert-heading simple-skeleton [:h3 text-node]))))))

;; Note: When you make a list of paragraphs for Strawberry Sunflower,
;; THERE ARE NEWLINES IN THE FREAKING LIST!!! In make-paragraphs I
;; used the paragraph as a function (since it's supposed to be a
;; map), leading to the famed ClassCastException.
(deftest test-make-paragraphs
  (testing "Normal operation, simple case"
    (is (= (result-string
            transform
            baremin-res
            [:.chapter :.chaptertext :p.standard]
            (make-paragraphs '({:tag :p
                                :content ["HELLO!!!!!!!!!"]})
                             identity))
           correct-simple)))
  (testing "When something in the paragraph list doesn't implement Ifn."
    (is (thrown-with-msg? clojure.lang.ExceptionInfo #"Raw paragraphs are invalid"
                          (make-paragraphs '({:tag :p, :content []} "\n") identity)))))

(deftest test-chapter
  (testing "Normal operation, chapter has heading"
    (let )))
