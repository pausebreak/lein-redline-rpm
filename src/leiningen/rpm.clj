(ns leiningen.rpm
  (:require
    [clojure.java.io :refer [file]]
    [leiningen.core.main :refer [info debug warn]])
  (:import (org.redline_rpm Builder)
           (org.redline_rpm.header Architecture RpmType Os)
           (org.redline_rpm.payload Directive)
           (java.io RandomAccessFile)))

(def hostname (.. java.net.InetAddress getLocalHost getHostName))
(def username (System/getProperty "user.name"))

(defn- rpm-path
  [spec]
  (str "target/rpm" (:version spec) ".rpm"))

(defn- create-directories
  [builder dirs]
  (when (seq dirs)
    (info "create-directories")
    (dorun (map
      (fn [d]
        (info "->" d)
        (let [[path mode user group] d
              directive (Directive.)]
          (.addDirectory builder path mode directive user group)))
      dirs))))

(defn- add-files
  [builder files]
  (when (seq files)
    (info "add-files")
    (dorun (map
      (fn [f]
        (info "->" f)
        (let [[path goes-to mode dir-mode user group] f]
          (.addFile builder goes-to (file path) mode dir-mode user group)))
      files))))

(defn- add-symlinks
  [builder links]
  (when (seq links)
    (info "add-symlinks")
    (dorun (map
      (fn [l]
        (info "->" l)
        (let [[source target] l]
          (.addLink builder target source))) ; yep the params are backwards from ln
      links))))

(defn rpm
  "Java based RPM generator"
  [{:keys [license description url version root rpm] :as project} & args]
  (let [filepath (rpm-path (cons project rpm))
        f (file filepath)
        file-channel (.getChannel (RandomAccessFile. f "rw"))
        pre-install-script (file (:pre-install-script rpm))
        post-install-script (file (:post-install-script rpm))
        pre-uninstall-script (file (:pre-uninstall-script rpm))
        post-uninstall-script (file (:post-uninstall-script rpm))
        builder (Builder.)]
    (doto builder
      (.setBuildHost hostname)
      (.setDescription description)
      (.setDistribution (:distribution rpm))
      (.setGroup (:group rpm))
      (.setLicense (:name license))
      (.setPackage (:package-name rpm) version (:release rpm))
      (.setPackager (:packager rpm))
      (.setPlatform (Architecture/valueOf "X86_64") (Os/valueOf "LINUX"))
      (.setSummary (:summary rpm));
      (.setType (RpmType/valueOf "BINARY"))
      (.setUrl url) (.setVendor (:vendor rpm))
      (.setPreInstallScript pre-install-script)
      (.setPostInstallScript post-install-script)
      (.setPreUninstallScript pre-uninstall-script)
      (.setPostUninstallScript post-uninstall-script)
      (.setPrefixes (into-array (:prefixes rpm)))
      (add-files (:files rpm))
      (create-directories (:directories rpm))
      (add-symlinks (:symlinks rpm))
      (.build file-channel))
    (info "Built: " filepath)))
