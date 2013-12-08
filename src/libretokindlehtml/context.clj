; context.clj
; For working on the REPL. Loads files for me to play around with.
(use 'net.cgrand.enlive-html)
(use 'clojure.java.io)
(use 'clojure.repl)


(def coolish (with-meta (mine-content "test-resources/testhtml/Coolish Walk.html") {:name "CoolishWalk"}))
(def c3 (with-meta (mine-content "resources/ofnight/Chapter 3.html") {:name "Chapter3",
                                                                      :position 3}))
(def trans (list-of-resources "test-resources/testhtml/config.json"))

(def valid-map {:directory "somewhere",
               :order ["f1", "f2", "f3"],
               :title "The title",
               :subtitle "The subtitle",
               :authors ["a1", "a2"],
               :template "path/to/template",
               :stylesheet "path/to/stylesheet"
               :mode "path/to.mode"})
