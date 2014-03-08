{
 :directory "resources/strawberrysunflower/"
 :order ["When will I make a friend 2.html", "My friend Lily.html", "My rival.html",
         "Dogs don't like me for some reason!.html", "Math is hard!.html", "The camping trip, part 1.html"],
 :title "Strawberry Sunflower Book 1",
 :subtitle "Sunshine in the Garden",
 :authors ["Trisdan Leyson"],
 :heading-selector [[:p (attr= :align "CENTER")]],
 :paragraph-selector [[:p (attr= :align "LEFT")]],
 :cleaner-reqs (require '[net.cgrand.enlive-html :refer :all])
 :cleaner (transformation
           [#{:span :font}]
           unwrap
           ;; unwrap pointless spans and fonts, which is all of them in StrawSun.
           [text-node]
           (do->
            #(clojure.string/replace % #"\p{Z}" " ")
            #(clojure.string/replace % #"^\s+" "")
            #(clojure.string/replace % #"--" "\u2014")))
}
