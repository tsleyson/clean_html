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
(defmacro pprint-str
  "Pretty prints forms to a string."
  [& forms]
  `(with-out-str (pprint ~@forms)))

(defn config-map
  "The fixture facility doesn't do exactly what I want so 
   this is my primitive workaround. Returns fake config data."
  []
  {:directory "test-resources/testdata/", 
   :order {"ClojureDocs - clojure.core_atom.html" 0
            "ClojureDocs - clojure.core_for.html" 1
            "ClojureDocs - clojure.pprint.html"   2} })

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
