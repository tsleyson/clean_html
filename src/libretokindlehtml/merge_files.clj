; merge_files.clj
; These functions are for merging a bunch of html files into
; one html file. You can get a list of html files turned into
; a list of enlive resources, and you can get them all merged
; into one html resource which can be written back to a file
; by write-resource.
(ns libretokindlehtml.merge-files
    (:require [libretokindlehtml.config-reader :refer [read-config]]
              [net.cgrand.enlive-html :as enlive]))

; have to find some way to sort the files so they're in the correct order.
; The built-in sort sorts them lexicographically, so Chapter 10 
; comes after Chapter 1.
; Here's the idea:
; - Read the ordering from somewhere. For now I'm going to parse
; it out of the file name with regular expressions, but for
; e.g. Strawberry Sunflower that wouldn't work, so try to 
; leave an option to take the ordering as a map from somewhere.
; - Attach the position of the file in the ordering as metadata
; on the resource when the list of resources is constructed.
; - Write a comparator that sorts based on metadata position.
; - Sort and merge into a single html file.
; - Profit!!

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
  "Returns a list of html resources from the files in the config map's :order field."
  type)

(defmethod list-of-resources clojure.lang.PersistentArrayMap
  [config]
  (let [{order :order, direc :directory} config
        files (keys order)]
    (sort-by #((meta %) :position) 
             (map #(with-meta (enlive/html-resource (clojure.java.io/file (str direc %)))
                     {:position (get order %), :name %})
                  files))))
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

; In the merge code, the head file is the first file in the
; list of resources. It's called that because it's the only
; one that keeps its head tag.
(defn mine-content 
  "Mines content of body tag to merge with head file." 
  [page] 
  (enlive/at (enlive/select page [:html :body]) [:head] nil [:body] enlive/unwrap))

; Will mine all content and append to content of first given page.
(defn merge-resources
  "Merges all resources in a given list into the first resource."
  [resource-list]
  (let [[head & subords] resource-list]
    (enlive/transform head [:body] (apply enlive/append (map mine-content subords)))))
