(ns libretokindlehtml.test.config-reader
  (:require [libretokindlehtml.config-reader :refer :all]
            [clojure.test :refer :all]
            [libretokindlehtml.test.core :refer [config-map]]))

(deftest test-validate-config
  (let [valid {:directory "somewhere",
               :order ["f1", "f2", "f3"],
               :title "The title",
               :subtitle "The subtitle",
               :authors ["a1", "a2"],
               :template "path/to/template",
               :stylesheet "path/to/stylesheet"}
        invalid {:directory "somewhere",
                 :order ["f1", "f2", "f3"],
                 :titlo "The titlo",
                 :subtitle "The subtitle",
                 :authors ["a1", "a2"],
                 :stylesheet "path/to/stylesheet"}]
    (is (= nil (validate-config invalid)))
    (is (= valid (validate-config valid)))))
