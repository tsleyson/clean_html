(ns libretokindlehtml.ofnight
  "Defines snippets and templates specifically for the conversion
   from Libre Office html to nice html for the book Of Night."
  (require [net.cgrand.enlive-html :refer :all]
           [clojure.java.io :refer [file]]
           [clojure.pprint :refer [pprint]]))

; Three things we'll need in here:
; Cleanup functions: transformations on the original html that clear out things like
; pointless spans and extra line breaks.
; Snippets: definitions for semantic elements of the text, like chapter, table of
; contents, etc.
; Template: The final template.

; snippets here.
(defsnippet chapter (file "resources/ofnight/chaptersnip.html") [:div.chapter]
  [[header & body]]
  [:span.chapheading] (let [cname (first (select header [[:h3 first-of-type] :> text-node]))]
                             ; I've no idea what :> does but altogether we cut out
                             ; all the crap and get just the text node.
                        (html-content (str "<a id='" cname "'>" cname "</a>")))

  [:div#chaptertext :p.standard]  (clone-for [para body]
                                             [:p.standard] (content (para :content))))
