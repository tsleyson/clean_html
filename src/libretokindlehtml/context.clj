; context.clj
; For working on the REPL. Loads files for me to play around with.
(use 'net.cgrand.enlive-html)
(use 'clojure.java.io)
(use 'clojure.repl)


(def coolish (with-meta (mine-content "test-resources/testhtml/Coolish Walk.html") {:name "CoolishWalk"}))
(def c3 (with-meta (mine-content "resources/ofnight/Chapter 3.html") {:name "Chapter3",
                                                                      :position 3}))
(def trans (list-of-resources "test-resources/testhtml/config.json"))
