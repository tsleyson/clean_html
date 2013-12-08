(ns libretokindlehtml.test.merge-files
  (require [clojure.test :refer :all]
           [clojure.java.io :refer [file reader writer]]
           [libretokindlehtml.merge-files :refer :all]
           [libretokindlehtml.test.core :refer [config-map check-meta]]))

(deftest test-list-of-resources
  (let [correct-str (slurp "test-resources/listofres.txt")
        result (list-of-resources (config-map))
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
