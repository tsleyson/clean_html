; context.clj
; For working on the REPL. Loads files for me to play around with.
(use 'net.cgrand.enlive-html)
(use 'clojure.java.io)
(use 'clojure.repl)
(use 'clojure.string)

(def coolish (with-meta (mine-content "test-resources/testhtml/Coolish Walk.html") {:name "CoolishWalk"}))
(def c3 (with-meta (mine-content "resources/ofnight/Chapter 3.html") {:name "Chapter3",
                                                                      :position 3}))
(def c1 (with-meta (mine-content "resources/ofnight/Chapter 1.html") {:name "Chapter1",
                                                                      :position 1}))
; for the <shift><tab> shit.
(def c8 (with-meta (mine-content "resources/ofnight/Chapter 8.html") {:name "Chapter8"
                                                                      :position 8}))
; To get the style information.
(def c1st (-> "resources/ofnight/Chapter 1.html"
              (file)
              (html-resource)
              (select [:style])
              (first)
              (:content)
              (first)))
(def plist  
  (map #(split % #"\{|\}") (filter #(when-not (= "" %) true) (split c1st #"\n\t"))))

(def have-style 
  (filter #(let [[_ styles] %] 
             (when (and styles (re-find #"(font-weight:bold)|(font-style:italic)" styles)) true)) 
          plist))

(def with-keywords (map #(let [[class style-str] %] [[(keyword (trim class))] style-str]) have-style))


(def trans (list-of-resources "test-resources/testhtml/config.json"))

(def valid-map {:directory "somewhere",
               :order ["f1", "f2", "f3"],
               :title "The title",
               :subtitle "The subtitle",
               :authors ["a1", "a2"],
               :template "path/to/template",
               :stylesheet "path/to/stylesheet"
               :mode "path/to.mode"})
