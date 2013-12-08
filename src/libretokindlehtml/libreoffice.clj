(ns libretokindlehtml.libreoffice
  "Defines cleaning functions specifically for the conversion
   from Libre Office html to nice html. (Libre Office generates
   different HTML if you choose Save as HTML from the menu;
   this is for HTML generated using libreoffice --headless
   --convert-to html on the command line.)"
  (require [net.cgrand.enlive-html :refer :all]
           [clojure.java.io :refer [file]]
           [clojure.pprint :refer [pprint]]))

; cleanup functions. The convention that I'm establishing is that
; cleanup functions should be named <thing they clean up>-maid, because
; I like maids. I bet Mahoro-san was programmed in Clojure. Rich Hickey
; probably worked for Vesper and used an early version of Clojure to
; program her.
; By the way, <thing they clean up> is whatever kind of thing you're
; passing to the snippet or template that uses the maid function.
; Here I called it paragraph-maid because it goes to the chapter
; snippet, and chapter receives paragraphs and uses this to clean
; them up. It doesn't have to be something you actually have a
; snippet for, just any logical division of the text.

(defn paragraph-maid
  "Runs a suite of cleanup functions on the given paragraph."
  []
  (transformation 
   [:br] nil ; get rid of all br inside p tags.
   [:> [:span first-child (but (or (attr? :class) (attr? :id)))]] 
    unwrap ; unwrap pointless spans at top of paragraph.
   [:> text-node]
    #(clojure.string/replace % #"^[\p{Z}\s]+?" "") 
      ; remove extra whitespace at the start of a paragraph.
   ))

      ; Libre Office stupidly puts this in sometimes.
      ; For some unfathomable reason, doesn't work if you 
      ; just give it (clojure.string/trim).
; I have fathomed the reason.
; Libre Office puts in ascii character 160, the non-breaking space.
; clojure.string/trim doesn't count this as a space; it only recognizes
; actual spaces as spaces.
; In Java, \s doesn't match this (although it does in some other
; languages, e.g. Javascript). So we need the \p{Z}.
