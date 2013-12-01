; merge_files.clj
; These functions are for merging a bunch of html files into
; one html file. You can get a list of html files turned into
; a list of enlive resources, and you can get them all merged
; into one html resource which can be written back to a file
; by write-resource.
(in-ns 'libretokindlehtml.core)

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

(defn extract-fname
  "Extracts a file name from a path. Tosses extension too."
  [path]
  (re-find #"(?<=/)[^/.]+(?=\.)" path))

(defn list-of-resources
  "Returns a list of html resources from the files in the config map's :order field."
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

(defn write-resource
  "Writes an Enlive resource, optionally to a file"
  ([resource]
  (apply str (enlive/emit* resource)))
  ([resource f]
  (spit f (apply str (enlive/emit* resource)))))


; note: doesn't quite work yet. Doesn't get rid of html tag.
(defn mine-content 
  "Mines content of body tag to merge with head file." 
  [page] 
  (enlive/at page [[:head :dtd]] nil [[:html :body]] enlive/unwrap))

; Will mine all content and append to content of first given page.
(defn merge-resources
  "Merges all resources in a given list into one."
  [resource-list]
  ())
