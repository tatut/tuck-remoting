(ns tuck.remoting
  "Define Tuck events that are sent to the server."
  (:require [tuck.core :as t]
            [clojure.spec.alpha :as s]))

(s/def ::client-id any?)
(s/def ::e! fn?)
(s/def ::client (s/keys :req [::client-id ::e!]))


(defprotocol ServerEvent
  (process-event [this client server-state]
    "Process this event received from the client and return new server state.
    `client` is map describing the client who sent this event.
    `context` is the user defined server context"))

(defmulti map->event ::event-type)

(defn- cljs? [env]
  (boolean (:ns env)))

(defmacro define-server-event
  "Define an event record that is automatically sent to the server.

  - `event-record-name` must be a symbol defining the event name.
  - `options` is a map of parameters

  The event is sent to the server. If the server is expected to send some other
  event as a reply to this event, a timeout may be specified. By default, no timeout
  is applied.

  Each event has an id (sequence number). If `event-id-path` is set, the id of the
  sent event will be assoc'ed to the app state at that path. This is useful for
  example for showing a loading indicator in the client.

  Supported options:

  - `timeout` number of milliseconds to wait for server to send a response
  - `on-timeout` a symbol denoting an event record to send if no response is received
  - `event-id-path` a vector denoting a path in the app state to set the event id to

  "
  {:doc/format :markdown}
  [event-record-name options]
  (assert (symbol? event-record-name) "Event record name must be a symbol.")
  (assert (map? options) "Options must be a map")
  (let [event-type (str *ns* "." event-record-name)
        event (gensym "event")
        app (gensym "app")
        event-id (gensym "event-id")]
    (if (cljs? &env)
      `(extend-type ~event-record-name
         tuck.core/Event
         (~'tuck.core/process-event [~event ~app]
           (let [~event-id (send-event! {:tuck.remoting/event-type ~event-type
                                         :tuck.remoting/event-args (into {} ~event)})]
             ;; FIXME: implement timeout
             ~(if-let [p (:event-id-path options)]
                `(assoc-in ~app ~p ~event-id)
                app))))
      `(defmethod map->event ~event-type [event#]
         (~(symbol (str "map->" event-record-name))
          (:tuck.remoting/event-args event#))))))



(defmacro define-client-event
  "Define event record that the server can send to the client.
  The must be previously defined record type."
  ([event-record-name]
   `(define-client-event
     ~event-record-name
     ~(symbol (str "map->" event-record-name))))
  ([event-record-name constructor]
   (let [event (gensym "event")]
     (when (cljs? &env)
       `(defmethod tuck.remoting/map->event ~(str *ns* "." event-record-name) [~event]
          (~constructor (:tuck.remoting/event-args ~event)))))))
