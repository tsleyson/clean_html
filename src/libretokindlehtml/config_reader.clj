; config_reader.clj
; Functions that read the configuration files. The config files are
; written in JSON and decoded using clojure.data.json
(in-ns 'libretokindlehtml.core)

(defn order-map
  "Returns a map giving the ordering for the chapters."
  [key value]
  (if (= key :order)
    (let [chapter-ordering {}]
      (pprint value)
      (->> (map #(assoc chapter-ordering %1 %2)
                value
                (range 0 (count value)))
           (apply merge {}))
    value)))

(defn read-config
  "Reads json from config file to Clojure map."
  [config-file]
  (with-open [r (reader config-file)]
    (doseq [line (line-seq r)]
      0)))
