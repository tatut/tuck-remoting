(ns tuck.remoting.server
  "Tuck remoting http-kit websocket server"
  (:require [tuck.remoting :as tr]
            [tuck.remoting.transit :as transit]
            [org.httpkit.server :as server :refer [with-channel on-close on-receive send!]]
            [clojure.string :as str]))

(defn read-message [data]
  (let [event-map (transit/transit->clj data)]
    (assert (map? event-map) "Received event that is not a map")
    event-map))

(defn make-handler [{:keys [context-fn on-connect-event on-close-event]}]
  (fn tuck-remoting-handler [request]
    (let [context (context-fn request)
          client-id (str (java.util.UUID/randomUUID))]
      (with-channel request channel


        (on-close channel (fn [status]
                            (when on-close-event
                              (tr/process-event (on-close-event status)
                                                {::tr/client-id client-id
                                                 ::tr/e! #(throw
                                                           (ex-info
                                                            "Client connection has been closed"
                                                            {::tr/client-id client-id}))}
                                                context))))
        (let [e! #(send! channel
                         (transit/clj->transit
                          {::tr/reply-to event-id
                           ::tr/event-type (str/replace (.getName (type %)) \_ \-)
                           ::tr/event-args (into {} %)}))]
          (when on-connect-event
            (tr/process-event (on-connect-event)
                              {::tr/client-id client-id
                               ::tr/e! e!}
                              context))
          (on-receive channel
                      (fn [data]
                        (let [{::tr/keys [event-id] :as msg} (read-message data)
                              event (tr/map->event msg)]
                          (tr/process-event event {::tr/client-id client-id
                                                   ::tr/e! e!} context)))))))))

(defn server [options]
  (server/run-server (make-handler options) {:port (:port options)}))
