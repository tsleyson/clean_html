(ns libretokindlehtml.merge-files
  (:require [net.cgrand.enlive-html :as enlive]))

; have to find some way to sort the files so they're in the correct order.
; The built-in sort sorts them lexicographically, so Chapter 10 
; comes after Chapter 1.
; Here's the idea:
; - Read the ordering from somwhere. For now I'm going to parse
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
  "Returns a list of html resources from the given directory."
  [directory]
  (map enlive/html-resource (.listFiles (clojure.java.io/file directory))))
