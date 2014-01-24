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

(defmacro redir
  "Redirects output to named file."
  [filename & body]
  `(with-open [w# (writer ~filename)]
     (binding [*out* w#] ~@body)))

(defn usage
  []
  (println "Give me the directory where your config.json file is stored.")) 

; Remember, the horror of forward declarations holds in Clojure, 
; so just put everything before main.
(defn -main
  [& args]
  (if (not= 1 (count args))
    (usage)
    (try 
      (println "Reading configuration from" (first args))
      (let [config (read-config (first args))
            fname (template-main config)]
           (println (str "Successfully wrote " fname ".")))
         (catch java.io.FileNotFoundException fnfe
           (do
             (println (str "Couldn't open file " (first args) "; not found."))
             (.printStackTrace fnfe))))))
; To do in here:
; Add some nice error messages.
