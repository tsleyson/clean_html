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

; cleanup functions.
(defn remove-br-in-p
  "Removes any <br> tag inside a <p> tag." ; with a very elispy name.
  [page]
  (transform [:p :br] nil))

; snippets here.
(defsnippet chapter (file "resources/ofnight/chaptersnip.html") [:div.chapter]
  [[header & body] & cleanup]
  [:span.chapheading] (let [cname (first (select header [[:h3 first-of-type] :> text-node]))]
                             ; I've no idea what :> does but altogether we cut out
                             ; all the crap and get just the text node.
                        (html-content (str "<a id='" cname "'>" cname "</a>")))
  [:div#chaptertext :p.standard]  (clone-for [para body]
                                             [:p.standard] (content ((if cleanup
                                                                       (apply comp cleanup)
                                                                       identity)
                                                                     (para :content)))))

; cleanup is a collection of functions to clean up the paragraph text. In this case
; we'll probably want to unnest from those pointless spans and get rid of all the
; pointless brs inside the paragraphs. Since I couldn't figure out any nicer way to do
; it (using a let wasn't really nicer), I used an if that returns the composition of
; all the cleanup functions if the cleanup argument has been passed, and otherwise uses
; the identity function.
