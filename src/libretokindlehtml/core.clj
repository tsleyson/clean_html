(ns libretokindlehtml.core
  (:refer-clojure)
  (:require [net.cgrand.enlive-html :as enlive]
            [clojure.data.json :as json]
            [libretokindlehtml.merge-files :refer :all]
            [libretokindlehtml.config-reader :refer :all]
            [libretokindlehtml.libreoffice :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :refer [file reader writer]]))

(defn usage
  []
  (println "Give me the directory where your config.json file is stored.")) 

; Remember, the horror of forward declarations holds in Clojure, 
; so just put everything before main.
(defn -main
  [& args]
  (if (not= 1 (count args))
    (usage)
    (do
      (let [res (list-of-resources (first args))]
        (pprint res)))))

; for test usage: lein run resources

