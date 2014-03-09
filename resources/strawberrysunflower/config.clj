{
 :directory "resources/strawberrysunflower/"
 :order ["When will I make a friend 2.html", "My friend Lily.html", "My rival.html",
         "Dogs don't like me for some reason!.html", "Math is hard!.html", "The camping trip, part 1.html"],
 :title "Strawberry Sunflower Book 1",
 :subtitle "Sunshine in the Garden",
 :authors ["Trisdan Leyson"],
 ;; Selecting text-node ditches pointless span and font tags inside heading..
 :heading-selector [[:p (attr= :align "CENTER")] text-node],
 :paragraph-selector [[:p (attr= :align "LEFT")]],
 :cleaner (transformation
           ;; unwrap pointless spans and fonts, which is all of them in StrawSun.
           [#{:span :font}]
           unwrap
           ;; Try to get rid of br inside paragraphs again.
           [:p :br]
           ;; Text-level cleaning.
           [text-node]
           (do->
            #(string/replace % #"\p{Z}" " ")
            #(string/replace % #"^\s+" "")
            #(string/replace % #"--" "\u2014")))
}
