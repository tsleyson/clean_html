(ns libretokindlehtml.test.novel
  "Some functions in novel aren't private (although they probably should have been)
and call for really complex tests, so they get their own file and don't have to
share template-helpers."
  (:require [libretokindlehtml.novel :refer :all]
            [net.cgrand.enlive-html :refer [html-resource transform content]]
            [clojure.test :refer [testing deftest use-fixtures is]]
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

(defn setup
  [test]
  (def baremin-res (html-resource (clojure.java.io/file "resources/templates/chaptersnip.html")))
  (def correct-simple (result-string transform
                       baremin-res
                       [:.chapter :.chaptertext :p.standard] 
                       (content "HELLO!!!!!!!!!")))
  
  (test))

(use-fixtures :each setup)

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
           correct-simple))))
