(ns chat.main
  (:require [reagent.core :as r]
            [tuck.core :as t]
            [tuck.remoting :as tr]
            [chat.events :as ev]
            [figwheel.client :as fw]
            [datafrisk.core :as d]
            [clojure.string :as str]))

(defonce app (r/atom {:name ""
                      :joined? false
                      :messages []
                      :compose-message nil}))

(defn append-msg [messages msg]
  (conj (if (> (count messages) 30)
          (subvec messages 1)
          messages) msg))

(extend-protocol t/Event
  ev/UpdateName
  (process-event [{name :name} app]
    (assoc app :name name))

  ev/UpdateComposedMessage
  (process-event [{msg :msg} app]
    (assoc app :compose-message msg))

  ev/Joined
  (process-event [{name :name} app]
    (-> app
        (update :recipients (fnil conj #{}) name)
        (assoc :joined? (or (:joined? app)
                            (= name (:name app))))
        (update :messages (fnil append-msg [])
                {:type :joined :participant name})))

  ev/Message
  (process-event [msg app]
    (update app :messages (fnil append-msg []) msg))

  ev/Parted
  (process-event [{name :name} app]
    (-> app
        (update :recipients (fnil disj #{}) name)
        (update :messages (fnil append-msg [])
                {:type :parted :participant name}))))

(defn chat-ui [e! {:keys [messages compose-message]}]
  [:div
   (doall
    (map-indexed
     (fn [i msg]
       ^{:key i}
       [:div.message
        (case (:type msg)

          :joined
          [:b (str "*** " (:participant msg) " joined the chat")]

          :parted
          [:b (str "*** " (:participant msg) " left the chat")]

          ;; default
          [:div
           [:span {:style {:font-size "75%"}}
            "[" (str (:time msg)) "] "]
           "<" [:b (:name msg)] "> " (:message msg)])])
     messages))
   [:input {:value compose-message
            :on-change #(e! (ev/->UpdateComposedMessage (-> % .-target .-value)))
            :on-key-press (fn [e]
                            (when (and (= "Enter" (.-key e))
                                       (not (str/blank? compose-message)))
                              (e! (ev/->Say compose-message))
                              (e! (ev/->UpdateComposedMessage ""))))}]])

(defn chat [e! {:keys [name joined?] :as app}]
  [:div
   (if-not joined?
     [:div
      "Join chat with name: "
      [:input {:type "text"
               :value name
               :on-change #(do (.log js/console %) (e! (ev/->UpdateName (-> % .-target .-value))))}]
      [:button {:on-click #(e! (ev/->Join name))} "join"]]

     [chat-ui e! app])

   [d/DataFriskShell app]])

(defn reload-hook []
  (r/force-update-all))

(defn ^:export start []
  (fw/start {:on-jsload reload-hook})
  (tr/initialize-client! "ws://localhost:9090" app)
  (r/render [t/tuck app chat]
            (.getElementById js/document "app")))
