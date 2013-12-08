(ns libretokindlehtml.novel
  "Contains templates and snippets needed for a novel.
   - chapter
   - toc (table of contents)
   - title (title page)
   - novel (merges all of the above)"
  (:require [net.cgrand.enlive-html :refer :all]
            [clojure.java.io :refer [file reader writer]]
            [clojure.string :as s]
            [libretokindlehtml.merge-files :refer [mine-all]]))
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

(defn- to-id
  "Replaces characters not allowed in HTML id attribute"
  [string]
  (try (s/replace string #"[\s:.,;\"']" "_")
       (catch java.lang.NullPointerException npe 
         (do
           (println (str "Arg is nil? " (if (nil? string) "yes" "no")))
           (.printStackTrace npe)
           (throw npe)))))

(defn- remove-extension
  "Removes the file extension."
  [filename]
  (s/replace filename #"\.[^.]+$" ""))

(defn- insert-heading
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
; So right now the most natural way to call it is (map chapter (list-of-resources config)).
; But I don't like that.
; You still have to call it like that, but novel takes care of that now
; so you can just call (novel config material).
(defsnippet chapter (file "resources/templates/chaptersnip.html") [:.chapter]
  [paragraphs & cleanup]
  [:#heading] (insert-heading paragraphs)
  [:#chaptertext :p.standard]  (let [[_ & body] paragraphs
                                        maid (if (nil? cleanup)
                                               identity
                                               (first cleanup))]
                                    (clone-for [para body]
                                               [:p] (content (maid (para :content))))))

; Note that I had to select :p under the clone-for because I used to have :p.standard
; but you're actually selecting from the resource, not the template.

; Expects the ordered list of chapters, with metadata, provided by list-of-resources.
(defsnippet toc (file "resources/templates/toc.html") [:#table_of_contents]
  [order-list]
  [:li] (let [cname-to-id (comp to-id :name meta)] 
                (clone-for [chapter order-list]
                           [:li] (set-attr :id (str "toc_entry_" (cname-to-id chapter)))
                           [:li :a] (do-> (content (remove-extension (:name (meta chapter))))
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

; expects a metadata map with title, subtitle (i.e. series title or
; numbering) and authors (a list).
(defsnippet title (file "resources/templates/title.html") [:body :#title_page]
  [{title :title, subtitle :subtitle, authors :authors}]
  [:#main_title] (content title)
  [:#subtitle] (content subtitle)
  [:.author] (clone-for [author authors]
                        [:.author] (content author)))

(deftemplate novel (file "resources/templates/novel.html")
  [config chapters]
  [:head :title] (content (str (:title config) " " (:subtitle config)))
  [:#main_text] (do-> 
                 (before (toc chapters))
                 (before (title config))
                 (content (map #(chapter % (libretokindlehtml.libreoffice/paragraph-maid)) chapters))))

(defn template-main
  "Assembles the text into its final form."
  [config]
  (let [text (mine-all config)
        html (apply str (novel config text))]
    (with-open [w (writer (str (:directory config) (:title config) ".html"))]
      (.write w html))))
