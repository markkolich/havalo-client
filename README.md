# havalo-client

A robust client for the <a href="https://github.com/markkolich/havalo#api">Havalo K,V store RESTful API</a>. 

Written in Java, but Scala compatible.

## Latest Version

The latest stable version of this library is <a href="http://markkolich.github.com/repo/com/kolich/havalo-client/0.0.7">0.0.7</a>.

## Resolvers

If you wish to use this artifact, you can easily add it to your existing Maven or SBT project using <a href="https://github.com/markkolich/markkolich.github.com#marks-maven2-repository">my GitHub hosted Maven2 repository</a>.

### SBT

```scala
resolvers += "Kolich repo" at "http://markkolich.github.com/repo"

val havaloClient = "com.kolich" % "havalo-client" % "0.0.7" % "compile"
```

### Maven

```xml
<repository>
  <id>Kolichrepo</id>
  <name>Kolich repo</name>
  <url>http://markkolich.github.com/repo/</url>
  <layout>default</layout>
</repository>

<dependency>
  <groupId>com.kolich</groupId>
  <artifactId>havalo-client</artifactId>
  <version>0.0.7</version>
  <scope>compile</scope>
</dependency>
```

## Usage

TODO

## Building

This Java library and its dependencies are built and managed using <a href="https://github.com/harrah/xsbt">SBT 0.12.1</a>.

To clone and build havalo-client, you must have <a href="http://www.scala-sbt.org/release/docs/Getting-Started/Setup">SBT 0.12.1 installed and configured on your computer</a>.

The havalo-client SBT <a href="https://github.com/markkolich/havalo-client/blob/master/project/Build.scala">Build.scala</a> file is highly customized to build and package this Java artifact.  It's written to manage all dependencies and versioning.

To build, clone the repository.

    #~> git clone git://github.com/markkolich/havalo-client.git

Run SBT from within havalo-client.

    #~> cd havalo-client
    #~/havalo-client> sbt
    ...
    havalo-client:0.0.7>

You will see a `havalo-client` SBT prompt once all dependencies are resolved and the project is loaded.

In SBT, run `package` to compile and package the JAR.

    havalo-client:0.0.7> package
    [info] Compiling 12 Java sources to ~/havalo-client/target/classes...
    [info] Packaging ~/havalo-client/dist/havalo-client-0.0.7.jar ...
    [info] Done packaging.
    [success] Total time: 4 s, completed

Note the resulting JAR is placed into the **havalo-client/dist** directory.

To create an Eclipse Java project for havalo-client, run `eclipse` in SBT.

    havalo-client:0.0.7> eclipse
    ...
    [info] Successfully created Eclipse project files for project(s):
    [info] havalo-client

You'll now have a real Eclipse **.project** file worthy of an Eclipse import.

Note your new **.classpath** file as well &mdash; all source JAR's are fetched and injected into the Eclipse project automatically.

## Dependencies

Currently, this artifact is built around my <a href="https://github.com/markkolich/kolich-httpclient4-closure">kolich-httpclient4-closure</a> library.

It also firmly depends on my common package of utility classes, <a href="https://github.com/markkolich/kolich-common">kolich-common</a>.

## Licensing

Copyright (c) 2012 <a href="http://mark.koli.ch">Mark S. Kolich</a>

All code in this artifact is freely available for use and redistribution under the <a href="http://opensource.org/comment/991">MIT License</a>.

See <a href="https://github.com/markkolich/havalo-client/blob/master/LICENSE">LICENSE</a> for details.
