; config_reader.clj
; Functions that read the configuration files. The config files are
; written in JSON and decoded using clojure.data.json
(in-ns 'libretokindlehtml.core)

;; (defn order-map
;;   "Returns a map giving the ordering for the chapters."
;;   [key value]
;;   (if (= key :order)
;;     (->> (let [chapter-ordering {}]
;;            (pprint value)
;;            (map #(assoc chapter-ordering %1 %2)
;;                 value
;;                 (range 0 (count value))))
;;          (apply merge {}))
;;     value)))

(defn order-map
  "Returns a map giving the ordering of the chapters. Value-fn for json/read."
  [key value]
  (if (= key :order)
      (loop [ordering {}, pos 0, chapters value]
        (if (= pos (count value))
          ordering
          (recur (assoc ordering (first chapters) pos) (inc pos) 
                  (rest chapters))))
      value))
; Loop/recur is sort of clunky in general, but this is shorter, cleaner,
; and more elegant than the above one, and as a bonus it gets the data in
; the right format the first time rather than do a second pass to clean
; up its mess, so here's a win for loop and recur.

(defn read-config
  "Reads json from config file to Clojure map."
  [config-file]
  (with-open [r (reader config-file)]
    (json/read r :key-fn keyword :value-fn order-map)))

; The tests brought up something--if we read the files in from a directory,
; the directory name will be appended to the front of the file.
; Fortunately we have the directory as part of the config JSON so just
; slap it on the front when we build the order map.
