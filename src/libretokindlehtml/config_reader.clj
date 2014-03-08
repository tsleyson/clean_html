; config_reader.clj
; Functions that read the configuration files. The config files are
; written in JSON and decoded using clojure.data.json
(ns libretokindlehtml.config-reader
  (:require [clojure.set :as set]
            [net.cgrand.enlive-html :refer :all]))  ; For evaling cleaners.

;; Here are the fruits of my time spent on 4clojure--compare with
;; version in config_reader_json file.
(defn order-map
  "Returns a map giving the ordering of the chapters."
  [config]
  (assoc config :order (into {} (map-indexed #(vector %2 %1) (:order config)))))

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
                     :heading-selector, :paragraph-selector, :cleaner,
                     :cleaner-reqs}
        requ-keys #{:directory, :order, :paragraph-selector}
        map-keys (into #{} (keys config-map))
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
                                   (remove true?)
                                   (interpose "\n")
                                   (apply str))})))))

(defn read-config-file
  "Reads Clojure map from config file using read-string. Does no
validation and keeps :order of chapters as vector."
  [config-file]
  (try (read-string (slurp config-file))
       (catch java.io.FileNotFoundException fnfe
         (do
           (.printStackTrace fnfe)
           (println)
           (throw fnfe)))))

(defn read-config
  "Reads a map using read-config-file, validates, and transforms :order."
  ;; order-map replaces the vector with a map giving the chapter ordering. Save
  ;; validation by requiring the ordering as a vector and not a map.
  [config-path]
  (when-let [cf (validate-config (order-map (read-config-file config-path)))]
    cf))
; Throws an exception from inside validate-map when
; invalid.
