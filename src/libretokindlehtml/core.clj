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
; But at doesn't chain the transformations; it just does each one
; separately, so if you need them to happen in order it's no good.
; Use do-> for cases like that. do-> returns a closure around your
; transformations (in a sequence) that you can apply to your nodes.
; It can be passed to clone-for, e.g., or just used like a normal
; function.
; ex.
; ((enlive/do-> (enlive/content "Nukem!!") (enlive/set-attr :id 22)) (enlive/select coolish [:p.P2]))
; (enlive/do-> (enlive/content "Nukem!!") (enlive/set-attr :id 22)) defines a closure that
; will replace the content of whatever nodes it's given with "Nukem!!" and set their id
; attributes to 22. Then we apply that to all the p nodes with class P2, selected from the
; resource coolish.

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

