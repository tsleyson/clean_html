; merge_files.clj
; These functions are for merging a bunch of html files into
; one html file. You can get a list of html files turned into
; a list of enlive resources, and you can get them all merged
; into one html resource which can be written back to a file
; by write-resource.
(ns libretokindlehtml.merge-files
  "Merge multiple html files into one file, get the content of the body
   tag, or get a list of files as Enlive resources."
    (:require [libretokindlehtml.config-reader :refer [read-config]]
              [net.cgrand.enlive-html :as enlive]
              [clojure.string :as s]
              [clojure.java.io :refer [file]]
              [clojure.test :refer [testing is with-test]]))

(def test-data 
  "\n\t@page {  }\n\ttable { border-collapse:collapse; border-spacing:0; empty-cells:show }\n\ttd, th { vertical-align:top; font-size:12pt;}\n\th1, h2, h3, h4, h5, h6 { clear:both }\n\tol, ul { margin:0; padding:0;}\n\tli { list-style: none; margin:0; padding:0;}\n\t<!-- \"li span.odfLiEnd\" - IE 7 issue-->\n\tli span. { clear: both; line-height:0; width:0; height:0; margin:0; padding:0; }\n\tspan.footnodeNumber { padding-right:1em; }\n\tspan.annotation_style_by_filter { font-size:95%; font-family:Arial; background-color:#fff000;  margin:0; border:0; padding:0;  }\n\t* { margin:0;}\n\t.P1 { font-size:12pt; font-family:Times New Roman; writing-mode:page; font-style:normal; }\n\t.P2 { font-size:14pt; font-weight:bold; margin-bottom:0.212cm; margin-top:0.423cm; font-family:Times New Roman; writing-mode:page; text-align:center ! important; }\n\t.Standard { font-size:12pt; font-family:Times New Roman; writing-mode:page; }\n\t.T1 { font-style:italic; }\n\t.T2 { font-style:normal; }\n\t.T3 { vertical-align:super; font-size:58%;font-style:normal; }\n\t<!-- ODF styles with no properties representable as CSS -->\n\t{ }\n\t")

(defn as-resource
  "Takes a path and returns an opened Enlive HTML resource."
  [path]
  (enlive/html-resource (file path)))

(defn to-selector-style-pair
  "Takes a pair of strings; returns an Enlive
   selector for a tag and the string that
   specifies that tag's style."
  [[class style-str]]
  [[(keyword (clojure.string/trim class))] style-str])
(-> (defn style-string
      "Gets the content of the style tag as a string.
       Output is suitable for extract-styles."
      [page]
      (if (not (seq? page))
        (throw (ex-info "style-string says: input arg is not an Enlive resource."
                        {:type "Invalid argument"
                         :cause "Didn't open as Enlive resource."}))
        (-> page
            (enlive/select [:style])
            (first)
            (:content)
            (first))))
    (with-test
      (testing "Basic usage"
        (is (= (style-string (as-resource "resources/ofnight/Chapter 1.html")) 
               test-data)))))

(-> (defn extract-styles
      "Returns the style information from an HTML resource as
       a vector of vectors containing an Enlive selector for the
       original CSS selector and a string of style commands.\n\n
       look-for is a regular expression to look for in the style
       string. If given, any elements whose style string doesn't
       match something in look-for are discarded."
      ; s/split throws a null pointer error when vintage-style
      ; isn't a string (say if we forget to open the resource).
      ([vintage-style]
         (->> (s/split vintage-style #"[}{]")
             (partition 2)
             (map to-selector-style-pair)))
      ([vintage-style look-for]
         (letfn [(match-look-for
                   [[_ styles]]
                   (when (and styles
                              (re-find look-for styles))
                     true))]
           (->> (s/split vintage-style #"[}{]")
               (partition 2)
               (filter match-look-for)
               (map to-selector-style-pair)))))
    (with-test
      (testing "Just bold and italics"
        (is (= (extract-styles (style-string (as-resource "resources/ofnight/Chapter 1.html"))
                               #"font-(weight:bold|style:italic)")
               '([[:.P2] 
                 " font-size:14pt; font-weight:bold; margin-bottom:0.212cm; margin-top:0.423cm; font-family:Times New Roman; writing-mode:page; text-align:center ! important; "]
                [[:.T1] " font-style:italic; "]))))))

(defmulti list-of-resources
  "Returns a list of html resources from the files in the config map's :order field.
   Can take either an already-read config map or a path to a config file."
  type)

(defmethod list-of-resources clojure.lang.PersistentArrayMap
  [config]
  (let [{order :order, direc :directory} config
        files (keys order)]
    (->> files
     (map #(-> (str direc %)
                   (as-resource)
                   (with-meta 
                     {:position (get order %), 
                      :name %})))
     (sort-by #((meta %) :position)))))
    ; opens the files, using the full path, but uses just the file name
    ; as the name metadatum and to get the position. Then sorts the
    ; resulting list by metadata position.

(defmethod list-of-resources java.lang.String
  [path-to-config]
  (list-of-resources (read-config path-to-config)))

; Still needs to be fleshed out (low priority)
(defn write-template
  "Writes the return value of an Enlive template to a file,
   with decent formatting."
  [template-out]
  )

; In the merge code, the head file is the first file in the
; list of resources. It's called that because it's the only
; one that keeps its head tag.
(defmulti mine-content
  "Mines content of body tag. Has the following signatures:
   - [html resource]
   - [file]
   - [path to a file (string)]"
  type)

(defmethod mine-content clojure.lang.LazySeq
  [page] 
  (enlive/at (enlive/select page [:html :body]) [:head] nil [:body] enlive/unwrap))

(defmethod mine-content java.io.File
  [page]
  (mine-content (enlive/html-resource page)))

(defmethod mine-content java.lang.String
  [page]
  (mine-content (-> page (clojure.java.io/file) (enlive/html-resource))))

(defn mine-all 
  "Obtains a list of resources using list-of-resources and
   returns the mined content of all resources in that list.
   Preserves metadata. Takes same argument types as list-of-resources."
  [config]
  (map #(with-meta (mine-content %) (meta %)) (list-of-resources config)))
