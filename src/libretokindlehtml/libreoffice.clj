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

(def wrap-standard (wrap :p {:class "standard"}))

(defn paragraph-maid
  "Runs a suite of cleanup functions on the given paragraph."
  []
  (transformation 
   {[:br] [:span]} #(-> % (second) (wrap-standard))
    ; Meant to fix that <shift><ret> crap where it uses a br
    ; instead of a new paragraph.
   [:span (but (attr? :style))]
    unwrap
    ; unwrap pointless spans.
    [text-node]
   (do->
    #(clojure.string/replace % #"\p{Z}" " ")
    #(clojure.string/replace % #"^\s+" "")
    #(clojure.string/replace % #"--" "\u2014")
    )
      ; remove extra whitespace at the start of a paragraph.
   ))

      ; Libre Office stupidly puts this in sometimes.
      ; For some unfathomable reason, doesn't work if you 
      ; just give it (clojure.string/trim).
; I have fathomed the reason.
; Libre Office puts in ascii character 160, the non-breaking space.
; (Also known as ascii char A0 in hexadecimal.
; clojure.string/trim doesn't count this as a space; it only recognizes
; actual spaces as spaces.
; In Java, \s doesn't match this (although it does in some other
; languages, e.g. Javascript). So we need the \p{Z}. See Mastering
; Regular Expressions on Unicode code points for how it works.

;— This is the em-dash character.

; How I fixed all the problems with the text node cleaners
; (might be important in the future)
; a) \p{Z} is a Unicode code point for spaces, as mentioned. So the first
;  clause was supposed to replace all weirdo spaces with normal spaces. It
;  didn't work. This is because I had [:> text-node] and the function was
;  working with p.standard so it didn't see anything wrapped in a span.
;  But Libre Office by default is totally stupid with the spans; it'll
;  make one just to have a CSS target to italicize some text, then just
;  leave it there. So the text-nodes inside spans weren't being targeted.
;  Now they are. That also causes problems because of the next item.
; b) I got rid of all whitespace at the beginning of a line. Elsewhere it
;  couldn't be assumed useless because Libre Office would leave whitespace
;  that I wanted outside of the span tags it used as targets for italics.
;  Sometimes, though, it leaves them inside and then the tag has leading
;  whitespace that I do want. I think the only way to solve this is to
;  write a CSS parser and parse the style tag in each of the files to figure
;  out which classes are actually italicized or bolded. (I found a CSS parser
;  on GitHub but it looks kind of chinsy. Plus I could use the practice.)
;  Then I can unwrap all the spans and divs that aren't being used for anything
;  and collapse all the ones that are into a single span, or else change it
;  to an <em> or <strong> tag.
; I used this process because Libre Office sometimes turns spaces I do want
; into non-breaking spaces. With this I can target just the spaces at the
; beginning, and also get
; c) Enlive automatically escapes, so you can't just insert &mdash; like I
;  wanted. Instead I added a meta tag and an xml declaration (Kindlegen
;  needs both for some reason) that specify the charset as UTF-8. This
;  takes care of em dashes and Erich von Dannekin, so I just replace
;  all the -- that Libre Office didn't with a proper em-dash.
