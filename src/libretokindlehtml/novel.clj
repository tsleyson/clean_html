(ns libretokindlehtml.novel
  "Contains templates and snippets needed for a novel.
   - chapter
   - toc (table of contents)
   - title (title page)
   - novel (merges all of the above)"
  (:require [net.cgrand.enlive-html :refer :all]
            [clojure.java.io :refer [file]]
            [clojure.string :as s]))

; selector definitions

 ; Selects the text under the first h3 node.
(def select-chapter-head  [[:h3 first-of-type] :> text-node])
; Be aware that using :> might cause some trouble since it only
; matches direct children of the h3 and not any deeper descendants.

; snippets here.

(defn insert-heading
  "Extracts heading text and inserts into tag."
  [paragraphs]
  (let [[header & _] paragraphs,
        headtext (first (select header select-chapter-head))
        {name :name} (meta paragraphs)]
    (html-content (str "<a id=\"" (s/replace name #"[\s:.,;\"']" "") "\">" headtext "</a>"))))
; Since HTML ids can't have spaces, we're removing them all, along with all
; the other punctuation.
; This makes the heading's anchor name be the name given in the
; metadata file. Then the toc snippet can use the same name, since
; it too will have the metadata, and it can fill in its hrefs
; correctly.

(defsnippet chapter (file "resources/templates/chaptersnip.html") [:div.chapter]
  [paragraphs & cleanup]
  [:#heading] (insert-heading paragraphs)
  [:div#chaptertext :p.standard]  (let [[_ & body] paragraphs
                                        maid (if (nil? cleanup)
                                               identity
                                               (first cleanup))]
                                    (clone-for [para body]
                                               [:p] (content (maid (para :content))))))

; cleanup is a function to clean up the paragraph text. In this case
; we'll probably want to unnest from those pointless spans and get rid of all the
; pointless brs inside the paragraphs.
; Also note that I had to select :p under the clone-for because I used to have :p.standard
; but you're actually selecting from the resource, not the template.
; Also note that it might be wasteful to do the whole (if (nil? thing every time we do
; the loop, and you might look into using an outer let with identity to just bind the
; function once. But not now.
