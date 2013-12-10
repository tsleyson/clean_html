(ns libretokindlehtml.core
  (:refer-clojure)
  (:require [net.cgrand.enlive-html :refer :all]
            [clojure.data.json :as json]
            [libretokindlehtml.merge-files :refer :all]
            [libretokindlehtml.config-reader :refer :all]
            [libretokindlehtml.libreoffice :refer :all]
            [libretokindlehtml.novel :refer :all]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :refer [file reader writer]]))

(defn usage
  []
  (println "Give me the directory where your config.json file is stored.")) 

; Remember, the horror of forward declarations holds in Clojure, 
; so just put everything before main.
(defn -main
  [& args]
  (if (not= 1 (count args))
    (usage)
    (try (let [config (read-config (first args))]
           (do
             (template-main config)
             (println (str "Successfully wrote " (:title config) ".html."))))
         (catch java.io.FileNotFoundException fnfe
           (.printStackTrace fnfe)))))
; To do in here:
; Add some nice error messages.
