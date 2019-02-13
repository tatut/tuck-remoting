(ns tuck.remoting.transit
  (:require [cognitect.transit :as t]))


#?(:clj
   (defn clj->transit [data]
     (let [out (java.io.ByteArrayOutputStream.)]
       (t/write (t/writer out :json) data)
       (String. (.toByteArray out) "UTF-8")))

   :cljs
   (defn clj->transit [data]
     (t/write (t/writer :json) data)))

#?(:clj
   (defn transit->clj [data]
     (let [in (java.io.ByteArrayInputStream. (.getBytes data "UTF-8"))]
       (t/read (t/reader in :json))))

   :cljs
   (defn transit->clj [data]
     (t/read (t/reader :json) data)))
