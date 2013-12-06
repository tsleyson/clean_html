(ns libretokindlehtml.novel
  "Contains templates and snippets needed for a novel.
   - chapter
   - toc (table of contents)
   - title (title page)
   - novel (merges all of the above)"
  (:require [net.cgrand.enlive-html :refer :all]
            [clojure.java.io :refer [file]]
            [clojure.string :as s]))
; Note to future self: I think the snippets you're defining are
; too complicated because they're doing too much at once. It would
; be simpler to make more of them and have several simple snippets
; punched together into more complicated ones. E.g. you could have
; had another snippet that defines the chapter heading, and just
; used it inside the chapter snippet. Something to think about.

; selector definitions

 ; Selects the text under the first h3 node. Made this a var
 ; so it can be redefined for other types of heading.
(def select-chapter-head  [[:h3 first-of-type] :> text-node])

; utilities

(defn to-id
  "Replaces characters not allowed in HTML id attribute"
  [string]
  (s/replace string #"[\s:.,;\"']" "_"))

(defn remove-extension
  "Removes the file extension."
  [filename]
  (s/replace filename #"\.[^.]+$" ""))

(defn insert-heading
  "Extracts heading text and inserts into tag."
  [paragraphs]
  (let [[header & _] paragraphs,
        headtext (first (select header select-chapter-head))
        {name :name} (meta paragraphs)]
    (html-content (str "<a id=\"" (to-id name) "\">" headtext "</a>"))))
    ; Sets anchor whose id is the chapter's name, read from the
    ; metadata. Used as target by table of contents.

; snippets

; Expects the output of mine-content, but with metadata, as its input.
(defsnippet chapter (file "resources/templates/chaptersnip.html") [:div.chapter]
  [paragraphs & cleanup]
  [:#heading] (insert-heading paragraphs)
  [:div#chaptertext :p.standard]  (let [[_ & body] paragraphs
                                        maid (if (nil? cleanup)
                                               identity
                                               (first cleanup))]
                                    (clone-for [para body]
                                               [:p] (content (maid (para :content))))))

; Note that I had to select :p under the clone-for because I used to have :p.standard
; but you're actually selecting from the resource, not the template.

; Expects the ordered list of chapters, with metadata, provided by list-of-resources.
(defsnippet toc (file "resources/templates/toc.html") [:#toc]
  [order-list]
  [:li] (let [cname-to-id (comp to-id :name meta)] 
                (clone-for [chapter order-list]
                           [:li] (set-attr :id (str "toc_entry_" (cname-to-id chapter)))
                           [:li :a] (comp (content (remove-extension (:name (meta chapter))))
                                          (set-attr :href (cname-to-id chapter))))))

; This works, at least in the repl.
;; (pprint (transform tocsnip [:#toc :li] 
;;                    (clone-for [elem trans] 
;;                               [:li] (set-attr :id (str "toc_entry_" 
;;                                                        (clojure.string/replace 
;;                                                         (:name (meta elem)) #"\s" "_"))) 
;;                               [:li :a] (comp (set-attr :href 
;;                                                        (clojure.string/replace 
;;                                                         (:name (meta elem)) #"\s" "_")) 
;;                                              (content (:name (meta elem)))))))

