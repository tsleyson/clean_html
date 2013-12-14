(ns libretokindlehtml.novel
  "Contains templates and snippets needed for a novel.
   - chapter
   - toc (table of contents)
   - title (title page)
   - novel (merges all of the above)"
  (:require [net.cgrand.enlive-html :refer :all]
            [clojure.java.io :refer [file reader writer]]
            [clojure.string :as s]
            [clojure.test :refer [with-test is testing]]
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

(-> (defn- remove-extension
      "Removes the file extension."
      [filename]
      (s/replace filename #"\.[^.]+$" ""))
    (with-test
      (testing "Normal usage"
        (is (= (remove-extension "blurgh.html") "blurgh")))
      (testing "Lots of dots in filename"
        (is (= (remove-extension "blurgh.barf.html.bak") "blurgh.barf.html")))
      (testing "No extension"
        (is (= (remove-extension "blurgh") "blurgh")))))

(-> (defn- to-id
      "Replaces characters not allowed in HTML id attribute
       and removes file extension."
      [string]
      (try (remove-extension (s/replace string #"[\s,;\"'?!]" "_"))
           (catch java.lang.NullPointerException npe 
             (throw npe))))
    (with-test 
      (testing "Replacing space, exclamation point, double quote, semicolon" 
        (is (= (to-id "!!Chapter 3\" ;dood") "__Chapter_3___dood")))
      (testing "Replacing single quote, comma, question mark"
        (is (= (to-id "Section'hey,where'smycar?") "Section_hey_where_smycar_")))
      (testing "Replacing nothing--this string is a valid id."
        (is (= (to-id "ValidID") "ValidID")))
      (testing "Throw an exception on nil"
        (is (thrown? java.lang.NullPointerException (to-id nil))))))

(defn- insert-heading
  "Extracts heading text and inserts into tag."
  [paragraphs]
  (let [[header & _] paragraphs,
        headtext (first (select header select-chapter-head))
        {name :name} (meta paragraphs)]
    (html-content (str "<a id=\"" (to-id name) "\">" headtext "</a>"))))
    ; Sets anchor whose id is the chapter's name, read from the
    ; metadata. Used as target by table of contents.

(defn make-paragraphs
  "Returns a function that generates paragraphs from given sequence 
   of Enlive data, with cleanup as its cleaner."
  [raws maid]
  (clone-for [para raws]
             (content (maid (para :content)))))

; snippets

; For when your chapter doesn't have a heading, e.g. Of Night's prologue.
(defsnippet no-heading (file "resources/templates/chaptersnip.html") [:.chapter]
  [raws maid]
  [:#chaptertext :p.standard] (make-paragraphs raws maid))

; Expects the output of mine-content, but with metadata, as its input.
; So right now the most natural way to call it is (map chapter (list-of-resources config)).
; But I don't like that.
; You still have to call it like that, but novel takes care of that now
; so you can just call (novel config material). Novel uses mine-all.
; Has problems if first line isn't heading. Use no-heading for that.
(defsnippet chapter (file "resources/templates/chaptersnip.html") [:.chapter]
  [paragraphs & cleanup]
  [:#heading] (insert-heading paragraphs)
  [:#chaptertext :p.standard]  (let [ [_ & body] paragraphs
                                      maid (if (nil? cleanup)
                                             identity
                                             (first cleanup))]
                                 (make-paragraphs body maid)))
; Note that I had to select :p under the clone-for because I used to have :p.standard
; but you're actually selecting from the resource, not the template.

; Expects the ordered list of chapters, with metadata, provided by list-of-resources.
(defsnippet toc (file "resources/templates/toc.html") [:#table_of_contents]
  [order-list]
  [:li] (let [cname-to-id (comp to-id :name meta)]
                (clone-for [chapter order-list]
                           [:li] (set-attr :id (str "toc_entry_" (cname-to-id chapter)))
                           [:li :a] (do-> (content (remove-extension (:name (meta chapter))))
                                          (set-attr :href (str "#" (cname-to-id chapter)))))))

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
  [:#front_matter] (content (title config) (toc chapters))
  [:#main_text] (content (no-heading (first chapters) (libretokindlehtml.libreoffice/paragraph-maid)) 
                         (map #(chapter % (libretokindlehtml.libreoffice/paragraph-maid)) (rest chapters))))

(defn template-main
  "Assembles the text into its final form."
  [config]
  (let [text (mine-all config)
        html (apply str (cons "<? xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                              (novel config text)))
        fname (str (:directory config)
                               (if-let [title (:title config)]
                                 (str title (:subtitle config))
                                 "default") ".html")]
    (do 
      (with-open [w (writer fname)]
          (.write w html))
      fname)))
