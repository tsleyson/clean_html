; merge_files.clj
; These functions are for merging a bunch of html files into
; one html file. You can get a list of html files turned into
; a list of enlive resources, and you can get them all merged
; into one html resource which can be written back to a file
; by write-resource.
(ns libretokindlehtml.merge-files
  "Merge multiple html files into one file, get the content of the body
   tag, or get a list of files as Enlive resources."
    (:require [libretokindlehtml.config-reader :refer [read-config]]
              [net.cgrand.enlive-html :as enlive]))

; Here's how to get just the children of the body tag:
; (enlive/at resource [#{:html :body}] enlive/unwrap [:head] nil)
; It still has the dtd and stuff.
; Note that putting the selected tags in a set is union: if a tag fits one
; or the other or both criterion, it's chosen. Putting them in a vector is
; intersection: a tag has to fulfill both criteria to be chosen. The above
; code works because it chooses anything which is an html tag or a body tag
; or both. Before I had a vector and it didn't work because the intersection
; is empty (nothing is both an html tag and a body tag).
; A way that gets rid of the dtd:
; (enlive/transform (enlive/select resource [:html :body]) [:body] enlive/unwrap)

(defn extract-fname
  "Extracts a file name from a path. Tosses extension too."
  [path]
  (re-find #"(?<=/)[^/.]+(?=\.)" path))

(defmulti list-of-resources
  "Returns a list of html resources from the files in the config map's :order field.
   Can take either an already-read config map or a path to a config file."
  type)

(defmethod list-of-resources clojure.lang.PersistentArrayMap
  [config]
  (let [{order :order, direc :directory} config
        files (keys order)]
    (->> (map #(with-meta (enlive/html-resource (clojure.java.io/file (str direc %)))
                     {:position (get order %), :name %})
              files) 
         (sort-by #((meta %) :position)))))
    ; opens the files, using the full path, but uses just the file name
    ; as the name metadatum and to get the position. Then sorts the
    ; resulting list by metadata position.

(defmethod list-of-resources java.lang.String
  [path-to-config]
  (list-of-resources (read-config path-to-config)))

(defn write-resource
  "Writes an Enlive resource, optionally to a file"
  ([resource]
  (apply str (enlive/emit* resource)))
  ([resource f]
  (spit f (apply str (enlive/emit* resource)))))

(defn write-template
  "Writes the return value of an Enlive template to a file,
   with decent formatting."
  [template-out]
  )

; In the merge code, the head file is the first file in the
; list of resources. It's called that because it's the only
; one that keeps its head tag.
(defmulti mine-content
  "Mines content of body tag. Has the following signatures:
   - [html resource]
   - [file]
   - [path to a file (string)]"
  type)

(defmethod mine-content clojure.lang.LazySeq
  [page] 
  (enlive/at (enlive/select page [:html :body]) [:head] nil [:body] enlive/unwrap))

(defmethod mine-content java.io.File
  [page]
  (mine-content (enlive/html-resource page)))

(defmethod mine-content java.lang.String
  [page]
  (mine-content (-> page (clojure.java.io/file) (enlive/html-resource))))

(defn mine-all 
  "Obtains a list of resources using list-of-resources and
   returns the mined content of all resources in that list.
   Preserves metadata. Takes same argument types as list-of-resources."
  [config]
  (map #(with-meta (mine-content %) (meta %)) (list-of-resources config)))
