(ns nightweb.actions
  (:require [nightweb.constants :as c]
            [nightweb.db :as db]
            [nightweb.formats :as f]
            [nightweb.io :as io]
            [nightweb.router :as router]
            [nightweb.torrents-dht :as dht]
            [nightweb.zip :as zip]))

(defn save-profile
  [{:keys [pic-hash name-str body-str]}]
  (let [profile (f/profile-encode name-str body-str pic-hash)]
    (db/insert-profile @c/my-hash-bytes (f/b-decode-map (f/b-decode profile)))
    (io/delete-orphaned-pics @c/my-hash-bytes)
    (io/write-profile-file profile)
    (future (router/create-meta-torrent))))

(defn new-post
  [{:keys [pic-hashes body-str ptr-hash ptr-time status create-time]}]
  (let [post (f/post-encode :text body-str
                            :pic-hashes pic-hashes
                            :status status
                            :ptrhash ptr-hash
                            :ptrtime ptr-time)
        file-name (or create-time (.getTime (java.util.Date.)))]
    (db/insert-post @c/my-hash-bytes
                    file-name
                    (f/b-decode-map (f/b-decode post)))
    (io/delete-orphaned-pics @c/my-hash-bytes)
    (io/write-post-file file-name post)
    (future (router/create-meta-torrent))))

(defn toggle-fav
  [{:keys [ptr-hash ptr-time]}]
  (let [content (db/get-single-fav-data {:userhash ptr-hash
                                         :time ptr-time})
        new-status (if (= 1 (:status content)) 0 1)
        fav-time (or (:time content) (.getTime (java.util.Date.)))
        fav (f/fav-encode ptr-hash ptr-time new-status)]
    (db/insert-fav @c/my-hash-bytes fav-time (f/b-decode-map (f/b-decode fav)))
    (io/write-fav-file fav-time fav)
    (dht/add-user-hash ptr-hash)
    (future (router/create-meta-torrent))))

(defn import-user
  [{:keys [source-str pass-str]}]
  (let [dest-str (c/get-user-dir)]
    (if (zip/unzip-dir source-str dest-str pass-str)
      (let [paths (set (zip/get-zip-headers source-str))
            new-dirs (-> (fn [d] (contains? paths (str d c/slash)))
                         (filter (io/list-dir dest-str)))]
        (if (router/create-imported-user new-dirs)
          nil
          :import_error))
      :unzip_error)))

(defn export-user
  [{:keys [dest-str pass-str]}]
  (let [source-str (c/get-user-dir @c/my-hash-str)]
    (io/delete-file dest-str)
    (when (zip/zip-dir source-str dest-str pass-str)
      dest-str)))