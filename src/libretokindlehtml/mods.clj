(ns libretokindlehtml.mods
  (:require [net.cgrand.enlive-html :refer :all]
            [clojure.java.io :refer [file]]))
; Inside the mods namespace, we'll let Enlive come all in.
; This could make it easier to write mod functions in the
; future.

; Neither of these functions work with Calibre. I can probably 
; hack something by changing the tags at the start of each
; resource to work with Calibre's automatic page breaks,
; though.
; Mobi doesn't display in Calibre the same way it does on the Fire.
; Try converting books using both of these methods and looking at
; them on the Fire.
(defn add-pgbreak-empty-div
  "Adds a pagebreak between elements by inserting an empty div
   and setting its style attribute. No brains at all; just shoves
   it at the beginning of whatever you pass in."
  [page-content]
  (cons {:tag :div, :attrs {:style "page-break-before: always;"}, :content: []}
        page-content))

(defn add-pgbreak-mb
  "Puts in a <mb:pagebreak /> tag. Currently doesn't work correctly."
  ; The issue is that Enlive doesn't recognize the tag as self-closing,
  ; so it actually puts in <mb:pagebreak></mb:pagebreak>. In emit-tag,
  ; which I assume is how emit* generates the tags, the tag is
  ; checked for membership in a set of self-closing tags. We could
  ; fix this by recompiling the source code with mb:pagebreak added
  ; to the set, or find some workaround for emit*.
  [page-content]
  (cons {:tag :mp:pagebreak, :content nil} page-content))

; stuff for the new template-based approach.
