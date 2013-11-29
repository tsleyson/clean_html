(ns libretokindlehtml.core
  (:require [net.cgrand.enlive-html :as enlive]
            [clojure.data.json :as json])
  (:use clojure.pprint clojure.java.io))
(load "merge_files")
(load "config_reader")

; to load an html resource
; (enlive/html-resource (java.net.URL. pathofresource))
; to select tags
; (enlive/select source [tags to select])
; to apply a transformation
; (enlive/transform resource [tags to select] transformation)
; for a list of transformations
 ;; (enlive/at source
 ;;            [tags1]
 ;;            transform1
 ;;            [tags2]
 ;;            transform2
 ;;            etc.)
; It's basically like (-> source (transform [tags1] etc.))

(defn usage
  []
  (println "Give me the directory where your html files are stored.")) 

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

