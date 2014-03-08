{:directory "C:/Users/trisdan/Dropbox/Writing/Succubus Story/Text/HTML2/",
 :order   ["Prologue.html", "Chapter 1.html", "Chapter 2.html",
 	   "Chapter 3.html", "Chapter 4.html", "Chapter 5.html",
	   "Chapter 6.html",
	   "Chapter 7.html", "Chapter 8.html", "Chapter 9.html",
	   "Chapter 10.html", "Chapter 11.html", "Chapter 12.html",
	   "Afterword.html"],
 :heading-selector [[:h3 first-of-type] :> text-node],
 :paragraph-selector [:p]
 :title "Of Night Book 1",
 :subtitle "Reversal of Expectations",
 :cleaner-reqs (require '[net.cgrand.enlive-html :refer :all])
 ;; :cleaner (transformation 
 ;;           {[:br] [:span]} #(-> % (second) ((wrap :p {:class "standard"})))
 ;;           ;; Meant to fix that <shift><ret> crap where it uses a br
 ;;           ;; instead of a new paragraph.
 ;;           [:span (but (attr? :style))]
 ;;           unwrap
 ;;           ;; unwrap pointless spans.
 ;;           [text-node]
 ;;           (do->
 ;;            #(clojure.string/replace % #"\p{Z}" " ")
 ;;            #(clojure.string/replace % #"^\s+" "")
 ;;            #(clojure.string/replace % #"--" "\u2014")
 ;;            )
 ;;                                        ; remove extra whitespace at the start of a paragraph.
 ;;           )
 ;; Breaks tests. See test/libretokindlehtml.config_reader.clj for reason.
}
