(ns leiningen.rpm
  (:require
   [clojure.java.io :refer [file]]
   [clojure.string :as string]
    [leiningen.core.main :refer [info debug warn]])
  (:import (org.redline_rpm Builder)
           (org.redline_rpm.header Header Flags Architecture RpmType Os)
           (org.redline_rpm.payload Directive)
           (java.io RandomAccessFile)))

(def hostname (.. java.net.InetAddress getLocalHost getHostName))
(def username (System/getProperty "user.name"))

(defn- rpm-path
  [package-name package-version]
  (str "target/" package-name "-" package-version ".rpm"))

(defn- create-directories
  [builder dirs]
  (when (seq dirs)
    (info "create-directories")
    (doseq [[path mode user group] dirs]
      (info "->" path mode user group)
      (.addDirectory builder path mode (Directive.) user group))))

(defn- add-files
  [builder files]
  (when (seq files)
    (info "add-files")
    (doseq [[path goes-to mode dir-mode user group] files]
      (info "->" path goes-to mode dir-mode user group)
      (.addFile builder goes-to (file path) mode dir-mode user group))))

(defn- destination<-
  [prefix root path]
  (str prefix (subs path (.length root))))

(defn- recurse-directories
  [builder dirs]
  (when (seq dirs)
    (info "add-directories")
    (doseq [[root prefix mode dir-mode user group] dirs]
      (info "->" root mode user group)
      (doseq [path (map #(.getPath %) (-> root file file-seq))
              :let [goes-to (destination<- prefix root path)]
              :when (-> path file .isDirectory not)]
        (do
           (info "->" path goes-to mode dir-mode user group)
           (.addFile builder goes-to (file path) mode dir-mode user group))))))

(defn- add-symlinks
  [builder links]
  (when (seq links)
    (info "add-symlinks")
    (doseq [[source target] links]
      (info "->" source target)
      (.addLink builder target source)))) ; yep the params are backwards from ln

(defn flag->int
  [f]
  (let [flags {">=" (bit-or Flags/GREATER Flags/EQUAL)
               "<=" (bit-or Flags/LESS Flags/EQUAL)
               "="  Flags/EQUAL
               ">>" Flags/GREATER
               "<<" Flags/LESS}]
    (if (contains? flags f)
      (int (get flags f))
      (throw (Exception. (str f " is not a valid flag"))))))

(defn- add-requires
  [builder requires]
  (when (seq requires)
    (info "add-requires")
    (doseq [[name flag version] requires]
      (info "->" name flag version)
      (.addDependency builder name (flag->int flag) version))))

(defn- add-conflicts
  [builder c]
  (when (seq c)
    (info "add-conflicts")
    (doseq [[name flag version] c]
      (info "->" name flag version)
      (.addConflicts builder name (flag->int flag) version))))

(defn- add-built-in-directories
  [builder dirs]
  (when (seq dirs)
    (info "add-built-in-directories")
    (doseq [dir dirs]
      (info "->" dir)
      (.addBuiltinDirectory builder dir))))

(defn- fix-package-version
  "Dashes are not allowed in RPM package version strings; that collides with
  Leiningen's `-SNAPSHOT` package versions.  To make building SNAPSHOT RPMs
  work, rewrite that portion of the package version."
  [package-version]
  (string/replace package-version
                  "-SNAPSHOT"
                  (str "SNAPSHOT" (.format (java.text.SimpleDateFormat. "yyyymmddHHMMSS") (new java.util.Date)))))

(defn rpm
  "Java based RPM generator"
  [{:keys [license description url version root rpm] :as project} & args]
  (let [package-version (fix-package-version (:version rpm version))
        filepath (rpm-path (:package-name rpm) package-version)
        f (file filepath)
        file-channel (.getChannel (RandomAccessFile. f "rw"))
        pre-install-script (file (:pre-install-script rpm))
        post-install-script (file (:post-install-script rpm))
        pre-uninstall-script (file (:pre-uninstall-script rpm))
        post-uninstall-script (file (:post-uninstall-script rpm))
        builder (Builder.)]
    (doto builder
      (add-built-in-directories (:built-in-directories rpm))
      (.setBuildHost hostname)
      (.setDescription description)
      (.setDistribution (:distribution rpm))
      (.setGroup (:group rpm))
      (.setLicense (:name license))
      (.setPackage (:package-name rpm) package-version (:release rpm))
      (.setPackager (:packager rpm))
      (.setPlatform (Architecture/valueOf "X86_64") (Os/valueOf "LINUX"))
      (.setSummary (:summary rpm))
      (.setType (RpmType/valueOf "BINARY"))
      (.setUrl url) (.setVendor (:vendor rpm))
      (.setPreInstallScript pre-install-script)
      (.setPostInstallScript post-install-script)
      (.setPreUninstallScript pre-uninstall-script)
      (.setPostUninstallScript post-uninstall-script)
      (.setPrefixes (into-array (:prefixes rpm [""])))
      (.setProvides (:provides rpm))
      (.setSourceRpm (:source-rpm rpm))
      (add-requires (:requires rpm))
      (add-conflicts (:conflicts rpm))
      (add-files (:files rpm))
      (create-directories (:directories rpm))
      (recurse-directories (:recurse rpm))
      (add-symlinks (:symlinks rpm))
      (.build file-channel))
    (info "Built: " filepath)))
