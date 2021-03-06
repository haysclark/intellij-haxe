Haxe plugin for Intellij IDEA
======================================

This plugin allows you to develop [Haxe](http://haxe.org/) programs with Intellij IDEA.
It requires Intellij IDEA Ultimate 13 or 14.

Install
-------

###Install from Intellij plugin repository
Use [Intellij plugin install documentation](https://www.jetbrains.com/idea/plugins/) to install the [Haxe plugin](https://plugins.jetbrains.com/plugin/6873?pr=idea).

###Install the latest or a previous Github release

Download the `intellij-haxe.jar` file from the release you want from [Github releases](https://github.com/TiVo/intellij-haxe/releases).
More recent releases have begun to be named `intellij-haxe-<release>.jar`, where &lt;release&gt; is the version of Idea for which the Jar is built.  (e.g. `intellij-haxe-14.1.1.jar`)
Make sure that you pick the proper one for your release.  A message should pop up and warn you if a release is incompatible.

In Intellij:
- open the Settings
- Go to plugin
- Click “install from disk”
- Select the “intellij-haxe.jar” file you downloaded

Build
-----

This describes the command line build on a Linux platform. To build from Intellij IDEA itself, see the [contributing](CONTRIBUTING.md) document to setup
your development environment.  Much more detail is provided there for command line build options as well.

###Dependencies
- Ant
- Oracle JDK 7 or Open JDK 7
- Make
- A bash compatible shell

###Build command
```
make
```

This will generate a `intelllij-haxe-<release>.jar` file at the root of the project that you can then install from disk
(see “Install the latest or a previous Github release).  Note that the default make will build the plugin for
Idea 13.1.6.  To override the default, set the IDEA_VERSION environment variable prior to executing make.

```
IDEA_VERSION=14.1.1 make
```

Note that building via make will download the requested version of IntelliJ Ultimate (to a temporary directory)
every time a build is started.  This can be quite slow at times.  For repeated building and testing, 
we recommended that you set up your machine as described in the [contributing document](CONTRIBUTING.md). 

Test
----

###Dependencies
Same as for build.

###Test command
```
make test
```

This will build and run the tests and display the JUnit report.  Again, you can override the Idea version
being tested against by overriding IDEA_VERSION.

```
IDEA_VERSION=14.1.1 make test
```


Use the hxcpp debugger
----------------------

The hxcpp debugger functionality has been rewritten to conform to the
Haxe v3.0 debugger.  In order to use this, you must:

- Install the newest debugger haxelib from https://github.com/TiVo/debugger
- Re-build your project using this newest debugger haxelib
- Configure your haxe program to start the debugger when the following
  command line option is provided:

```
-start_debugger
```
  (and if you expect to do remote debugging, you'll also have to support:

```
    -debugger_host=[host]:[port]
```

  Most likely you'll just want to add the following in your main() when
  `-start_debugger` is set:
    
```
    new debugger.Local(true);
```

Contribute
----------
See the [contributing document](CONTRIBUTING.md).
