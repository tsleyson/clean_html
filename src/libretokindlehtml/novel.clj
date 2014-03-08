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

;; WARNING!!!
;; I modified the code to build Strawberry Sunflower, because I'm a
;; lazy ass. Make sure to put it back for Of Night.

; selector definitions

 ; Selects the text under the first h3 node. Made this a var
 ; so it can be redefined for other types of heading.
;;(def select-chapter-head  [[:h3 first-of-type] :> text-node])

;; The Strawberry Sunflower heading selector and paragraph selector.
;; We have to get the text-node for the heading because we don't know
;; how many layers of pointless font and span tags are inside the
;; header's node. With the standard paragraphs we just shove in the
;; pointless 
(def select-chapter-head [[:p (attr= :align "CENTER")] text-node])
(def select-standard-paragraph [[:p (attr= :align "LEFT")]])

; utilities

(-> (defn- remove-extension
      "Removes the file extension."
      [filename]
      (if (nil? filename)
        (throw (ex-info "remove-extension says: filename is nil" {:type "Invalid argument"}))
        (s/replace filename #"\.[^.]+$" "")))
    (with-test
      (testing "Normal usage"
        (is (= (remove-extension "blurgh.html") "blurgh")))
      (testing "Lots of dots in filename"
        (is (= (remove-extension "blurgh.barf.html.bak") "blurgh.barf.html")))
      (testing "No extension"
        (is (= (remove-extension "blurgh") "blurgh")))
      (testing "Null input"
        (is (thrown-with-msg? clojure.lang.ExceptionInfo #"filename is nil" (remove-extension nil))))))

(-> (defn- to-id
      "Replaces characters not allowed in HTML id attribute
       and removes file extension."
      [string]
      (if (nil? string)
        (throw (ex-info "to-id says: string is nil" {:type "Invalid argument"}))
        (remove-extension (s/replace string #"[\s,;\"'?!]" "_"))))
    (with-test 
      (testing "Replacing space, exclamation point, double quote, semicolon" 
        (is (= (to-id "!!Chapter 3\" ;dood") "__Chapter_3___dood")))
      (testing "Replacing single quote, comma, question mark"
        (is (= (to-id "Section'hey,where'smycar?") "Section_hey_where_smycar_")))
      (testing "Replacing nothing--this string is a valid id."
        (is (= (to-id "ValidID") "ValidID")))
      (testing "Throw an exception on nil"
        (is (thrown-with-msg? clojure.lang.ExceptionInfo #"string is nil" (to-id nil))))))

(defn insert-heading
  "Extracts heading text and inserts into tag."
  [paragraphs heading-selector]
  (let [headtext (select paragraphs heading-selector)
        {name :name} (meta paragraphs)]
    (if (< 1 (count headtext))  ; 1 < count allows 0, i.e. no heading.
      (throw (ex-info "insert-heading says: selector yields non-unique answer."
                      {:type "Bad selector" :cause headtext}))
      (content {:tag :a, :attrs {:id (to-id name)}, :content [(first headtext)]}))))
    ; Sets anchor whose id is the chapter's name, read from the
    ; metadata. Used as target by table of contents.

; Later I want to be able to get rid of this.
(defn add-styles
  "Adds a style attribute to text to maintain bold and italics."
  [raws]
  (let [{style-list :styles} (meta raws)]
    (reduce #(let [[selec style-str] %2]
               (transform %1 selec (set-attr :style style-str)))
            raws
            style-list)))

(defn make-paragraphs
  "Returns a function that generates paragraphs from given sequence 
   of Enlive data, with cleanup as its cleaner."
  ;; If para doesn't have function nature (e.g. it's a string),
  ;; we get ClassCastException. We expect maps (Enlive tags).
  [raws maid]
  (if (not (every? ifn? raws))
    (throw (ex-info "make-paragraphs says: Raw paragraphs are invalid."
                    {:type "Bad paragraph in raws"
                     :cause "Paragraph does not implement Ifn, so is not a map."}))
    (clone-for [para raws]
               (content (maid (para :content))))))

; snippets

; For when your chapter doesn't have a heading, e.g. Of Night's prologue.
;; Should now be obsolete due to update to insert-heading and heading selection.
(defsnippet no-heading (file "resources/templates/chaptersnip.html") [:.chapter]
  [raws maid]
  [:.chaptertext :p.standard] (make-paragraphs raws maid))

; Expects the output of mine-content, but with metadata, as its input.
; So right now the most natural way to call it is (map chapter (list-of-resources config)).
; But I don't like that.
; You still have to call it like that, but novel takes care of that now
; so you can just call (novel config material). Novel uses mine-all.
;; DOES NOT correctly handle cases where there is text before the
;; chapter heading. To deal with this we would need a new snippet
;; with a paragraph tag before the heading tag (that we could hang
;; clones on) and a new defsnippet to populate them correctly. Not
;; gonna deal with it right now. 
(defsnippet chapter (file "resources/templates/chaptersnip.html") [:.chapter]
  [paragraphs config]
  [:.heading] (insert-heading paragraphs (:heading-selector config))
  [:.chaptertext :p.standard]  (let [raws (filter map? (select paragraphs (:paragraph-selector config)))
                                     body (add-styles 
                                            (with-meta raws (meta paragraphs)))
                                     maid (if-let [c (:cleaner config)]
                                             c
                                             identity)]
                                 (make-paragraphs body maid)))

; Expects the ordered list of chapters, with metadata, provided by list-of-resources.
(defsnippet toc (file "resources/templates/toc.html") [:#table_of_contents]
  [order-list]
  [:li] (let [cname-to-id (comp to-id :name meta)]
                (clone-for [chapter order-list]
                           [:li] (set-attr :id (str "toc_entry_" (cname-to-id chapter)))
                           [:li :a] (do-> (content (remove-extension (:name (meta chapter))))
                                          (set-attr :href (str "#" (cname-to-id chapter)))))))


; expects a metadata map with title, subtitle (i.e. series title or
; numbering) and authors (a list).
(defsnippet title (file "resources/templates/title.html") [:body :#title_page]
  [{title :title, subtitle :subtitle, authors :authors}]
  [:#main_title] (content title)
  [:#subtitle] (content subtitle)
  [:.author] (clone-for [author authors]
                        [:.author] (content author)))

;; The template novel for Of Night
;; (novel (file "resources/templates/novel.html")
;;   [config chapters]
;;   [:head :title] (content (str (:title config) " " (:subtitle config)))
;;   [:#front_matter] (content (title config) (toc chapters))
;;   [:#main_text] (content (no-heading (first chapters) (libretokindlehtml.libreoffice/paragraph-maid)) 
;;                          (map #(chapter % (libretokindlehtml.libreoffice/paragraph-maid)) (rest chapters))))

(deftemplate novel (file "resources/templates/novel.html")
  [config chapters]
  [:head :title] (content (str (:title config) " " (:subtitle config)))
  [:#front_matter] (content (title config) (toc chapters))
  [:#main_text] (content (map #(chapter % config) chapters)))

(defn template-main
  "Assembles the text into its final form."
  [config]
  (let [
        text (mine-all config)
        html (apply str (cons "<? xml version=\"1.0\" encoding=\"UTF-8\" ?>"
                              (novel config text)))
        fname (str (:directory config)
                               (if-let [title (:title config)]
                                 (str title " " (:subtitle config))
                                 "default")
                               ".html")]
    (do 
      (with-open [w (writer fname)]
          (.write w html))
      fname)))
