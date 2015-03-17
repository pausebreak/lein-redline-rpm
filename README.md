# lein-redline-rpm
A pure java RPM Leiningen plugin

Currently this is rather incomplete but there is enough to make a simple rpm.

## Usage

Add `lein-redline-rpm "0.1.0"` to `:plugins` in your `project.clj` as well as
configure `:rpm`

```
lein rpm
```

## Needs Done

Name the rpm based on the project with a configure and/or command line override.

Input validation. Redline does very little input validation and rarely throws.

Add Symlinks and all the other stuff not exposed yet.

More Documentation.
