# havalo-client

A client for the <a href="https://github.com/markkolich/havalo#api">Havalo K,V store RESTful API</a>.

Makes makes aggressive use of <a href="https://github.com/markkolich/kolich-httpclient4-closure">kolich-httpclient4-closure</a>, and uses the <a href="http://hc.apache.org/">Apache Commons HttpClient</a> version 4.2.2 under-the-hood. 

Written in Java, but Scala compatible.

## Latest Version

The latest stable version of this library is <a href="http://markkolich.github.com/repo/com/kolich/havalo-client/0.0.8">0.0.8</a>.

## Resolvers

If you wish to use this artifact, you can easily add it to your existing Maven or SBT project using <a href="https://github.com/markkolich/markkolich.github.com#marks-maven2-repository">my GitHub hosted Maven2 repository</a>.

### SBT

```scala
resolvers += "Kolich repo" at "http://markkolich.github.com/repo"

val havaloClient = "com.kolich" % "havalo-client" % "0.0.8" % "compile"
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
  <version>0.0.8</version>
  <scope>compile</scope>
</dependency>
```

## Usage

### Instantiate a HavaloClient

Before you can do anything, you'll need to instantiate a `HavaloClient` backed by an `HttpClient` instance.

You will pass your Havalo API access key and secret to the `HavaloClient` via its constructor.  The `HavaloClient` instance will handle all authentication with the API on your behalf.  Further, you'll need to provide the Havalo API endpoint URL.

Note, the API key and secret are created by the Havalo API when a new repository is created.  

```java
import com.kolich.havalo.client.service.HavaloClient;
import java.util.UUID;

final UUID key = ...; // Your Havalo API key (a UUID)
final String secret = ...; // Your Havalo API secret

// Your Havalo API endpoint URL, usually something like:
// http://localhost:8080/havalo/api
final String apiUrl = ...;

final HavaloClient client = new HavaloClient(key, secret, apiUrl);
```

If you have a configured `HttpClient` that you'd like to use instead of the default, you can of course use a slightly different constructor and pass your own.

```java
final HttpClient client = ...; // Your own HttpClient instance

final HavaloClient client = new HavaloClient(client, key, secret, apiUrl);
```

Finally, if you're using Spring, your web-application can also instantiate a `HavaloClient` bean.

```xml
<!-- Your own HttpClient instance -->
<bean id="HttpClient"
  class="com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory"
  factory-method="getNewInstanceWithProxySelector">
  <constructor-arg><value>Some kewl user-agent String</value></constructor-arg>
</bean>

<bean id="HavaloClient"
  class="com.kolich.havalo.client.service.HavaloClient">
  <constructor-arg index="0" ref="HttpClient" />
  <constructor-arg index="1"><value>6fe8e625-ec21-4890-a685-a7db4346cceb</value></constructor-arg>
  <constructor-arg index="2"><value>Crb7s5coXNb...EnQIYr-9cxNqShozksHitLg</value></constructor-arg>
  <constructor-arg index="3"><value>http://localhost:8080/havalo/api</value></constructor-arg>
</bean>
```

And, that's it!

### Using your HavaloClient

All `HavaloClient` methods return an `HttpResponseEither<F,S>` &mdash; this return type represents *either* a left type `F` indicating failure, or a right type `S` indicating success.  For more details on this return type and how to use it, please refer to the <a href="https://github.com/markkolich/kolich-httpclient4-closure#functional-concepts">Functional Concepts overview</a> in my <a href="https://github.com/markkolich/kolich-httpclient4-closure">kolich-httpclient4-closure</a> library.

#### authenticate()

Verify your Havalo API authentication credentials.

Does nothing other than verifies that your API key and secret work.  This is most useful on application startup when you want to verify connectivity/access to the Havalo API before attempting to do actual work. 

```java
final HttpResponseEither<HttpFailure,KeyPair> auth =
  client.authenticate();

if(auth.success()) {
  // Yay, it worked!
} else {
  // Authentication failed.
}
```

#### createRepository()

Create a new repository.

Note that only **administrator** level API users can create new repositories.

```java
final HttpResponseEither<HttpFailure,KeyPair> repo =
  client.createRepository();

final KeyPair kp;
if((kp = repo.right()) != null) {
  // Success.
  System.out.println("Your new API key: " + kp.getKey());
  System.out.println("Your new API secret: " + kp.getSecret());
}
```

#### deleteRepository(toDelete)

Delete repository by UUID `toDelete`.

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

List objects in repository, or list all objects in repository that start with a given prefix.

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
final HttpResponseEither<HttpFailure,List<Header>> get =
  client.getObject(os, "foobar", "baz", "0.json");

if(get.success()) {
  // Success, object data was copied to provided OutputStream.
  // Here are the HTTP headers on the response.
  final List<Header> headers = get.right();
  for(final Header h : headers) {
    System.out.println(h.getName() + ": " + h.getValue());
  }
} else {
  // Object not found.
}
```

#### getObjectMetaData(path...)

Get the meta data associated with the object at the given `path`.

Any HTTP headers sent with the object during a `PUT` are returned.  Note that the API generated SHA-1 hash that was computed on upload is also returned in the `ETag` HTTP response header.

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

Upload (`PUT`) and object to the given `path` that is `contentLength` bytes long, using the provided `inputStream`.  Send any additional meta data represented by `headers` with the request too.

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
  // Success!
  // The SHA-1 hash of the uploaded object can be found in the
  // ETag HTTP response header.
  final String hash = upload.right().getFirstHeader("ETag");
  System.out.println("Uploaded object SHA-1 hash is: " + hash);
}
```

**TIP:** For a conditional `PUT`, you can also send an `If-Match` HTTP request header with your request.  If the SHA-1 hash sent with the `If-Match` header matches the current SHA-1 hash of the object, the object will be *replaced*.  If the SHA-1 hash sent with the `If-Match` header does *not* match the current hash of the object, the `PUT` will fail with a `409 Conflict`. 

#### putObject(byte[], path...)

Upload (`PUT`) and object to the given `path` using the provided `byte[]` array.

```java
final byte[] data = ...; // Some byte[] array of your data.

// Upload an object to path "cat"
final HttpResponseEither<HttpFailure,FileObject> upload =
  client.putObject(data, "cat");

if(upload.success()) {
  // Success!
  // The SHA-1 hash of the uploaded object can be found in the
  // ETag HTTP response header.
  final String hash = upload.right().getFirstHeader("ETag");
  System.out.println("Uploaded object SHA-1 hash is: " + hash);
}
```

#### deleteObject(headers[], path...)

Delete an object at the given `path` only if the SHA-1 hash of that object matches the SHA-1 hash sent with the `If-Match` HTTP request header.

```java
import static org.apache.http.HttpHeaders.IF_MATCH;

// An SHA-1 hash for the version of the object you want to delete.
final String myHash = "de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3";

// Delete the object at path "foobar/cat" only if that object's hash
// equals "de9f2c7fd25e1b3afad3e85a0bd17d9b100db4b3".
final HttpResponseEither<HttpFailure,Integer> delete =
  client.deleteObject(
    new Header[]{new BasicHeader(IF_MATCH, myHash)},
    "foobar", "cat"
  );

if(delete.success()) {
  // Success!
} else {
  // Failed, get the resulting HTTP status code so
  // we can see what happened.
  final Integer result = delete.left();
  switch(result) {
    case 404:
      // HTTP/1.1 404 Not Found
      // Object at provided path didn't exist.
      // ...
      break;
    case 409:
      // HTTP/1.1 409 Conflict
      // Object conflict, the sent SHA-1 hash did not match.
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

Delete an object at the given `path`, ignoring the object's current hash.

```java
// Delete the object at path "foobar/cat".
final HttpResponseEither<HttpFailure,Integer> delete =
  client.deleteObject("foobar", "cat");

if(delete.success()) {
  // Success!
} else {
  // Failed
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
    havalo-client:0.0.8>

You will see a `havalo-client` SBT prompt once all dependencies are resolved and the project is loaded.

In SBT, run `package` to compile and package the JAR.

    havalo-client:0.0.8> package
    [info] Compiling 12 Java sources to ~/havalo-client/target/classes...
    [info] Packaging ~/havalo-client/dist/havalo-client-0.0.8.jar ...
    [info] Done packaging.
    [success] Total time: 4 s, completed

Note the resulting JAR is placed into the **havalo-client/dist** directory.

To create an Eclipse Java project for havalo-client, run `eclipse` in SBT.

    havalo-client:0.0.8> eclipse
    ...
    [info] Successfully created Eclipse project files for project(s):
    [info] havalo-client

You'll now have a real Eclipse **.project** file worthy of an Eclipse import.

Note your new **.classpath** file as well &mdash; all source JAR's are fetched and injected into the Eclipse project automatically.

## Licensing

Copyright (c) 2012 <a href="http://mark.koli.ch">Mark S. Kolich</a>

All code in this artifact is freely available for use and redistribution under the <a href="http://opensource.org/comment/991">MIT License</a>.

See <a href="https://github.com/markkolich/havalo-client/blob/master/LICENSE">LICENSE</a> for details.
