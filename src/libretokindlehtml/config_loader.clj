(ns libretokindlehtml.config-loader)

(defn setup-config-ns
  "Dynamically create config ns and populate with imported namespaces.
  Currently imports
  - clojure.core
  - Everything from net.cgrand.enlive-html
  - clojure.string as string"
  []
  (binding [*ns* *ns*]
    (in-ns 'libretokindlehtml.config)
    (refer-clojure)
    (require '[net.cgrand.enlive-html :refer :all]
             '[clojure.string :as string]))) ; Add extra imports here.
;; TODO Parameterized imports might be cool, no?

(defn load-config
  [path]
  (binding [*ns* *ns*]
    (in-ns 'libretokindlehtml.config)
    (load-file path)))

