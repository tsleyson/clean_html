; To call the network-main function as the main from Leiningen:
; lein run -m libretokindlehtml.network/network-main
(ns libretokindlehtml.network
  "This file allows the program to be called as a web service
   over a network. 

   A client HTML form accepts the information
   that would otherwise appear in the config file and sends
   it to the server we start up in here.

   The server processes the data and calls template-main
   according to the same instructions as core/-main. It then
   sends the produced file back to the client (for fun).
   I'm thinking we might load this in a new tab using Javascript."
  (:require [clojure.data.json :as json]
            [clojure.string :as s])
  (:import [org.webbitserver WebServer WebServers WebSocketHandler]
           [org.webbitserver.handler StaticFileHandler]))

(def connections (atom 0 :validator #(>= % 0)))

; Mock responder for testing.
(defn mock-response
  [msg]
  (str "We got:\n\t" 
       (:directory msg) "\n\t" 
       (pr-str (:order msg))))

(defn on-open
  [connection]
  (do
    (swap! connections inc)
    (let [open (str "Current open connections: " @connections)] 
      (.send connection (json/json-str {:type "open" :msg open}))
      (println open))))

(defn on-close
  [connection]
  (swap! dec connections))

(defn on-message
  "Responds to messages from the client."
  [connection msg]
  (let [message (-> msg json/read-json (get-in [:data :config]))]
    (.send connection (mock-response message))))

(defn network-main
  "Starts up a server process on port 8080 to accept 
   incoming data."
  []
  (do
    (println "Starting server on port 8080.")
    (doto (WebServers/createWebServer 8080)
      (.add "/web"
            (proxy [WebSocketHandler] []
              (onOpen [c] (on-open c))
              (onClose [c] (println c))
              (onMessage [c j] (println c j))))
      (.add (StaticFileHandler. "resources/web_interface"))
      (.start))))
