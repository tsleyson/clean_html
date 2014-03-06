; config_reader.clj
; Functions that read the configuration files. The config files are
; written in JSON and decoded using clojure.data.json
(ns libretokindlehtml.config-reader
  (:require [clojure.data.json :as json]
            [clojure.java.io :refer [reader writer file]]
            [clojure.set :as set]))

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
        (if (empty? chapters)
          ordering
          (recur (assoc ordering (first chapters) pos)
                 (inc pos) 
                 (rest chapters))))
      value))
; Loop/recur is sort of clunky in general, but this is shorter, cleaner,
; and more elegant than the above one, and as a bonus it gets the data in
; the right format the first time rather than do a second pass to clean
; up its mess, so here's a win for loop and recur.


; The tests brought up something--if we read the files in from a directory,
; the directory name will be appended to the front of the file.
; Fortunately we have the directory as part of the config JSON so just
; slap it on the front when we build the order map.

(defn- message-on-error
  "Returns true if its first argument is true,
   and otherwise returns the message."
  [no-error? message]
  (or no-error? message))

(defn validate-config
  "Returns config-map if a valid configuration map; 
   otherwise returns nil."
  [config-map]
  (let [valid-keys #{:directory, :order, :title, :subtitle, :authors,
                     :template, :stylesheet, :mode}
        requ-keys #{:directory, :order}
        map-keys (reduce conj #{} (keys config-map))
        messages (map message-on-error 
                    [(= valid-keys (set/union valid-keys map-keys)),
                     (= requ-keys (set/intersection requ-keys map-keys))]
                    ["Invalid key in map.",
                     (str "Missing required keys " 
                          (set/difference requ-keys map-keys) ".")])]
    (if (every? true? messages)
      config-map
      (throw (ex-info "validate-config says: Configuration map is invalid"
                      {:type "Bad config info",
                       :cause (->> messages
                                  (filter #(not (true? %)))
                                  (interpose "\n")
                                  (apply str))})))))
(defn read-config-file
  "Reads json from config file to Clojure map."
  [config-file]
  (try (with-open [r (reader config-file)]
         (json/read r :key-fn keyword :value-fn order-map))
       (catch java.io.FileNotFoundException fnfe
         (do
           (.printStackTrace fnfe)
           (println)
           (throw fnfe)))))

(defn read-config
  "Reads a json object using read-config-file and ensures
   the object is a valid config map."
  [config-path]
  (when-let [cf (validate-config (read-config-file config-path))]
    cf))
; Throws an exception from inside validate-map when
; invalid.
