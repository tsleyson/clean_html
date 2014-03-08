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

(defn equal-by-line
  "Compares two files line-by-line, using BufferedReader and line-seq."
  [f1 f2]
  (with-open [r1 (reader f1)
              r2 (reader f2)]
    (loop [s1 (line-seq r1), s2 (line-seq r2)]
      (cond 
       (not= (first s1) (first s2)) false
       (and (empty? s1) (empty? s2)) true
       (or (empty? s1) (empty? s2)) false
       :else (recur (rest s1) (rest s2))))))

; You also might want to consider writing a macro for this common pattern
; of setting up stuff in a let and then using a do form with a bunch of
; ises in it (yay, finally a use for macros!!)

;; It turns out they have one. It's called are.

; Helper functions for the tests.
(defmacro pprint-str
  "Pretty prints forms to a string."
  [& forms]
  `(with-out-str (pprint ~@forms)))


(defn check-name-and-pos
  "Checks whether a metadata map is correct."
  [obj metamap]
  (let [onobj (meta obj)]
    (and (= (:name onobj) (:name metamap))
         (= (:position onobj) (:position onobj)))))

(defn check-meta
  "Checks whether an entire metadata map is correct."
  [accused righteous]
  (= (meta accused) righteous)) ; Ye not guilty.

(deftest test-full-program
  (testing "Strawberry Sunflower basic HTML generation"
    (do (-main "resources/strawberrysunflower/config.clj")
        (is (true? (equal-by-line
                    "resources/strawberrysunflower/Strawberry Sunflower Book 1 Sunshine in the Garden.html"
                    "test-resources/testhtml/strawsunbk1.html"))))))
