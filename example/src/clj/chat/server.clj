(ns chat.server
  "Tuck remoting example: chat server"
  (:require [tuck.remoting.server :as tr-server]
            [tuck.remoting :as tr]
            [chat.events :as chat])
  (:gen-class))

(def clients (atom {}))

(defn send-all! [clients event]
  (doseq [{e! :e!} (vals @clients)]
    (e! event)))

(defn send-message! [clients name message]
  (send-all! clients (chat/->Message name (java.util.Date.) message)))

(extend-protocol tr/ServerEvent
  chat.events.Join
  (process-event [{name :name} {::tr/keys [client-id e!]} {clients :clients}]
    (println "[" client-id "] Joined chat: " name)
    (swap! clients assoc client-id {:name name
                                    :e! e!})
    (send-all! clients (chat/->Joined name)))

  chat.events.Say
  (process-event [{m :message} {::tr/keys [client-id e!]} {clients :clients}]
    (let [who (-> clients deref (get client-id) :name)]
      (send-message! clients who m)))

  chat.events.Disconnected
  (process-event [{status :status} {client-id ::tr/client-id} {clients :clients}]
    (let [who (-> clients deref (get client-id) :name)]
      (println "[" client-id "] Disconnected" status)
      (swap! clients dissoc client-id)
      (send-all! clients (chat/->Parted who)))))

(defn -main [& args]
  (tr-server/server {:on-close-event chat/->Disconnected
                     :context-fn (fn [req]
                                   {:clients clients})
                     :port 9090}))
