{
 :title "Simple Stuff"
 :subtitle "The First Test"
 :heading-selector [[:p (attr= :class "heading")] text-node]
 :paragraph-selector [[:p (attr= :class "standard")]]
 :directory "test-resources/snippettests"
 :order {"ch1.html" 0, "ch2.html" 1, "ch3.html" 2, "ch4.html", 3}
 :cleaner-reqs (require '[net.cgrand.enlive-html :refer :all])
 :cleaner (transformation
           [text-node]
           #(clojure.string/replace % #",\s+" "--")
           [:span]
           unwrap)
}
