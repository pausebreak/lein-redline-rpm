# lein-redline-rpm

A pure java RPM Leiningen plugin

## Usage

Add `lein-redline-rpm "0.3.0"` to `:plugins` in your `project.clj` as well as
configure `:rpm`

```
:rpm {:package-name "The name the rpm system uses"
      :distribution "The textual human name for this rpm"
      :summary "Whatever you want"
      :vendor "The name of the person or organization responsible"
      :packager "The name and possibly email address of the person who built this"
      :group "Application/System"
      :release "1"
      :pre-install-script "script/rpm/pre-install"
      :post-install-script "script/rpm/post-install"
      :pre-uninstall-script "script/rpm/pre-uninstall"
      :post-uninstall-script "script/rpm/post-uninstall"
      :prefixes ["/relocatable/paths"]
      :source-rpm "the-src.rpm"
      :conflicts [["httpd" ">=" "3.3"]]
      :provides ["webserver"]
      :requires [["nginx" ">=" "1.6.2"]]
      :files [["target/your.jar" "/opt/business/your.jar" 0640 0750 "user" "group"]]
      :symlinks [["/source" "/target"]]}

      ;; creates an empty directory
      :directories [["/this/directory/will/be/created" 0750 "user" "group"]]
```

To create the rpm:

```
lein rpm
```

## Needs Done

Name the rpm based on the project with a configure and/or command line override.

Input validation. Redline does very little input validation and rarely throws.

Add private-key stuff

Add directory traversal file addition ( the thing you thought .addDirectory() did ).

Tests? We could use the redline Scanner to validate various `:rpm` configurations.

Figure out what to do with redline's desire for slf4j. Currently ignoring.

## License

Distributed under the Eclipse Public License, the same as Clojure.
