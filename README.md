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

### 1 &ndash; Instantiate a HavaloClient

Before you can do anything, you'll need to instantiate a `HavaloClient` backed by an `HttpClient` instance.

You will pass your Havalo API access key and secret to the `HavaloClient` via its constructor.  The `HavaloClient` instance will handle all authentication with the API on your behalf.  Further, you'll need to provide the Havalo API endpoint URL.

Note, the API key and secret are created by the Havalo API when a new repository is created.  

```java
import com.kolich.havalo.client.service.HavaloClient;
import java.util.UUID;

final UUID key = ...; // Your Havalo API key (a UUID)
final String secret = ...; // Your Havalo API secret

final String apiUrl = ...; // Your Havalo API endpoint URL

final HavaloClient client = new HavaloClient(key, secret, url);
```

If you're using Spring, you can also instantiate a `HavaloClient` in the form a Bean.

```xml
<bean id="HavaloClient"
  class="com.kolich.havalo.client.service.HavaloClient">
  <constructor-arg index="0"><value>6fe8e625-ec21-4890-a685-a7db4346cceb</value></constructor-arg>
  <constructor-arg index="1"><value>Crb7s5coXNb...EnQIYr-9cxNqShozksHitLg</value></constructor-arg>
  <constructor-arg index="2"><value>http://localhost:8080/havalo/api</value></constructor-arg>
</bean>
```

That's it!

### 2 &ndash; Using the Client

All `HavaloClient` methods return an `HttpResponseEither<F,S>` &mdash; this return type represents *either* a left type `F` indicating failure, or a right type `S` indicating success.  For more details on this return type and how to use it, please refer to this <a href="https://github.com/markkolich/kolich-httpclient4-closure#functional-concepts">Functional Concepts overview</a> in my <a href="https://github.com/markkolich/kolich-httpclient4-closure">kolich-httpclient4-closure</a> library.

#### authenticate()

Verify your Havalo API authentication credentials.

```java
final HttpResponseEither<HttpFailure,KeyPair> auth = client.authenticate();

if(auth.success()) {
  System.out.println("Yay, it worked!");
}
```

#### createRepository()

Create a new repository.

Note that only **administrator** level API users can create new repositories.

```java
final HttpResponseEither<HttpFailure,KeyPair> repo = client.createRepository();

final KeyPair kp;
if((kp = repo.right()) != null) {
  // Success.
  System.out.println("Your new API key: " + kp.getKey());
  System.out.println("Your new API secret: " + kp.getSecret());
}
```

#### deleteRepository(toDelete)

Delete an existing repository.

Note that only **administrator** level API users can delete repositories.

```java
final UUID toDelete = ...; // The UUID of the repository to delete.

final HttpResponseEither<HttpFailure,Integer> del =
  client.deleteRepository(toDelete);

if(del.success()) {
  System.out.println("Deleted repository successfully, status code: " +
    del.right());
}
```

#### listObjects([prefix...])

List objects in *your* repository, or list all objects in *your* repository that start with a given prefix.

Note the `prefix` argument is optional &mdash; if omitted all objects will be returned.

```java
// List all objects whose key starts with "foobar/baz"
final HttpResponseEither<HttpFailure,ObjectList> list =
  client.listObjects("foobar", "baz");

if(list.success()) {
  System.out.println("Found " + list.getObjectList().size() + " objects.");
}
```

#### getObject(outputStream, path...)

Get an object with the given `path` and write it out to the provided `outputStream`.

```java
final OutputStream os = ...; // An existing and open OutputStream.

// Get the object whose key is "foobar/baz/0.json" and stream its
// bytes to the provided OutputStream.
final HttpResponseEither<HttpFailure,Long> get =
  client.getObject(os, "foobar", "baz", "0.json");

if(get.success()) {
  System.out.println("I copied " + get.right() + " total bytes.");
} else {
  System.out.println("Object not found?");
}
```

#### getObjectMetaData(path...)

Get the meta data associated with the object at the given `path`.

Any HTTP headers sent with the object during a `PUT` are returned.  Note that the API generated SHA-1 `ETag` that was computed on upload is also returned.

```java
// Get the meta data for the object whose key is "foobar/baz/1.xml"
final HttpResponseEither<HttpFailure,List<Header>> meta =
  client.getObjectMetaData("foobar", "baz", "1.xml");

if(meta.success()) {
  final List<Header> headers = meta.right();
  for(final Header h : headers) {
    System.out.println(h.getName() + ": " + h.getValue());
  }
}
```

#### putObject(inputStream, contentLength, headers, path...)

Upload (`PUT`) and object to the given `path` that is `contentLength` bytes long, using the provided `inputStream`.  Send any additional meta data represented by `headers` with the request.

```java
import static org.apache.http.HttpHeaders.CONTENT_TYPE;

final InputStream is = ...; // An existing and open InputStream.

// Upload an object to path "baz/foobar.jpg" .. since it's a JPG
// image, also send the relevant Content-Type with the request.
final HttpResponseEither<HttpFailure,FileObject> upload =
  client.putObject(is, 1024L,
    new Header[]{new BasicHeader(CONTENT_TYPE, "image/jpeg")},
    "baz", "foobar.jpg");

if(upload.success()) {
  // Upload successful.
  final String eTag = upload.right().getFirstHeader("ETag");
  System.out.println("Uploaded object SHA-1 hash is: " + eTag);
}
```

**TIP:** For a conditional `PUT`, you can also send an `If-Match` HTTP request header with your request.  If the SHA-1 hash sent with the request matches the current SHA-1 hash of the object, it will be *replaced*.  If the SHA-1 hash sent with the request does *not* match the current hash of the object, the `PUT` will fail with a `409 Conflict`. 

#### putObject(byte[], path...)

Upload (`PUT`) and object to the given `path` using the provided `byte[]` array.

```java
final byte[] data = ...; // Some byte[] array of your data.

// Upload an object to path "cat"
final HttpResponseEither<HttpFailure,FileObject> upload =
  client.putObject(data, "cat");

if(upload.success()) {
  // Upload successful.
  final String eTag = upload.right().getFirstHeader("ETag");
  System.out.println("Object SHA-1 ETag is: " + eTag);
}
```

#### deleteObject(headers[], path...)

Delete an object at the given `path` only if the ETag of that object matches the ETag provided by `ifMatch`.

```java
import static org.apache.http.HttpHeaders.IF_MATCH;

// An ETag for the object you want to delete.
final String ifMatch = "de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3";

// Delete the object at path "foobar/cat" only if that object's ETag
// equals "de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3".
final HttpResponseEither<HttpFailure,Integer> delete =
  client.deleteObject(
    new Header[]{new BasicHeader(IF_MATCH, ifMatch)},
    "foobar", "cat"
  );

if(delete.success()) {
  // Deletion successful.
} else {
  // Deletion failed, get the resulting HTTP status code
  // so we can see what exactly happened.
  final Integer result = delete.left();
  switch(result) {
    case 404:
      // HTTP/1.1 404 Not Found
      // Object at provided path didn't exist.
      // ...
      break;
    case 409:
      // HTTP/1.1 409 Conflict
      // Object conflict, the provided ETag did not match.
      // ...
      break;
    default:
      // Hmm, something else went wrong.
      // ...
      break;
  }
}
```

#### deleteObject(path...)

Delete an object at the given `path`, ignoring the ETag.

```java
// Delete the object at path "foobar/cat".
final HttpResponseEither<HttpFailure,Integer> delete =
  client.deleteObject("foobar", "cat");

if(delete.success()) {
  // Deletion successful.
} else {
  // Deletion failed.
  System.out.println("Oops, delete failed with status: " + delete.left());
}
```

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
