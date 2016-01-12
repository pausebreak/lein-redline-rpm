# lein-redline-rpm

A pure java RPM Leiningen plugin

## Usage

Add `lein-redline-rpm "0.4.2"` to `:plugins` in your `project.clj` as well as
configure `:rpm`

```
:rpm {:package-name "The name the rpm system uses"
      :distribution "The textual human name for this rpm"
      :summary "Whatever you want"
      :vendor "The name of the person or organization responsible"
      :packager "The name and possibly email address of the person who built this"
      :group "Application/System"
      :version 0.4.4   ;; if this is not set then the project :version is used
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
      :built-in-directories ["/opt/" "/usr/lib"]
      :files [["target/your.jar" "/opt/business/your.jar" 0640 0750 "user" "group"]]
      :recurse [["target/dir" "/opt/business/your/prefix" 0640 0750 "user" "group"]]
      :symlinks [["/source" "/target"]]}

      ;; creates empty directories
      :directories [["/this/directory/will/be/created" 0750 "user" "group"]]
```

To create the rpm:

```
lein rpm
```

## Things to Know

`:built-in-directories` is a optional vector of strings (paths) that will not be not be mananged by your RPM.
By default the parent directories of each file are added to your RPM unless they are in this vector or part of
the ["BUILTIN"](https://github.com/craigwblake/redline/blob/master/src/main/java/org/redline_rpm/payload/Contents.java#L49).

## Needs Done

Input validation. Redline does very little input validation and rarely throws.

Add private-key stuff

Tests? We could use the redline Scanner to validate various `:rpm` configurations.

Figure out what to do with redline's desire for slf4j. Currently ignoring.

## License

Distributed under the Eclipse Public License, the same as Clojure.
