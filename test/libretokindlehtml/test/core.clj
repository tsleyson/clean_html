(ns libretokindlehtml.test.core
  (:use [libretokindlehtml.core])
  (:use [clojure.test])
  (:use clojure.pprint)
  (:use net.cgrand.enlive-html)
  (:use clojure.java.io))

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
    (pprint (meta obj))
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
        result  (list-of-resources (config-map))]
    (do
      (is (= (pr-str result) correct-str))
      (let [metas [{:name "ClojureDocs - clojure.core_atom.html", 
                    :position 0}
                   {:name "ClojureDocs - clojure.core_for.html", 
                    :position 1}
                   {:name "ClojureDocs - clojure.pprint.html", 
                    :position 2}]]
        (is (every? identity (map check-meta result metas)))))))

(deftest test-mine-content
  (let [correct (slurp "test-resources/test-mine-content.txt")
        result  (-> "test-resources/testhtml/Coolish Walk.html"
                    (file)
                    (net.cgrand.enlive-html/html-resource)
                    (mine-content))]    
    (is (= (with-out-str (pprint result)) correct))))
