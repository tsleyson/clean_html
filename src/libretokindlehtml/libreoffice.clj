(ns libretokindlehtml.libreoffice
  "Defines snippets and templates specifically for the conversion
   from Libre Office html to nice html."
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

(defn libre-maid
  "Runs a suite of cleanup functions on the given paragraph."
  []
  (transformation 
   [:p :br] nil ; get rid of all br inside p tags.
   [[:span (but (or (attr? :class) (attr? :id)))]] 
    unwrap ; unwrap pointless spans at top of paragraph.
   ))

; snippets here.

(defsnippet chapter (file "resources/templates/chaptersnip.html") [:div.chapter]
  [[header & body] & cleanup]
  [:span.chapheading] (let [cname (first (select header [[:h3 first-of-type] :> text-node]))]
                             ; I've no idea what :> does but altogether we cut out
                             ; all the crap and get just the text node.
                        (html-content (str "<a id='" cname "'>" cname "</a>")))
  [:div#chaptertext :p.standard]  (clone-for [para body]
                                             [:p] (content (if (nil? cleanup) 
                                                             (para :content)
                                                             ((first cleanup) (para :content))))))

; cleanup is a function to clean up the paragraph text. In this case
; we'll probably want to unnest from those pointless spans and get rid of all the
; pointless brs inside the paragraphs.
; Also note that I had to select :p under the clone-for because I used to have :p.standard
; but you're actually selecting from the resource, not the template.
