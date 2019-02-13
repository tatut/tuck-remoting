(ns tuck.remoting
  (:require [tuck.core :as t]
            [tuck.remoting.transit :as transit]))

(def connection (atom nil))

(def event-id (atom 0))

(defmulti map->event :tuck.remoting/event-type)

(defn- receive [app-atom event]
  (let [e! (t/control app-atom)
        event-map (transit/transit->clj (.-data event))
        event (map->event event-map)]
    (assert (satisfies? t/Event event) "Not an event")
    ;(.log js/console "Got event: " (pr-str event))
    (e! event)))

(defn connect! [ws-url app-atom on-connect]
  (let [conn (js/WebSocket. ws-url)]
    (set! (.-onopen conn) on-connect)
    (set! (.-onmessage conn) (fn [event]
                               (when event
                                 (receive app-atom event))))
    (set! (.-onclose conn) (fn [event]
                             (.log js/console "WebSocket closed, reconnecting.")
                             (connect! ws-url app-atom)))
    (reset! connection conn)))

(defn initialize-client!
  "Initialize Tuck remoting client. Takes the websocket URL and the app state atom
  to apply received events against.

  Must be called before any events can be sent."
  [ws-url app-atom on-connect]
  (connect! ws-url app-atom on-connect))

(defn send-event!
  "Send event to Tuck remoting WS server.
  Returns event id."
  [event]
  (let [id (swap! event-id inc)
        event (assoc event :tuck.remoting/event-id id)
        conn @connection]
    (.log js/console "Sending event: " (pr-str event))
    (assert conn "No connection! Call initialize-client! before sending events.")
    (.send conn (transit/clj->transit event))
    id))
