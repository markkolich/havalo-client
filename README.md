# havalo-client

A client for the <a href="https://github.com/markkolich/havalo#api">Havalo K,V store RESTful API</a>.  Written in Java, but Scala compatible.

Makes aggressive use of <a href="https://github.com/markkolich/kolich-httpclient4-closure">kolich-httpclient4-closure</a> backed by the <a href="http://hc.apache.org/">Apache Commons HttpClient 4.x</a>.  Also, uses <a href="http://code.google.com/p/google-gson/">Google's GSON library</a> for all JSON related "stuph" under-the-hood.

## Latest Version

The latest stable version of this library is <a href="http://markkolich.github.com/repo/com/kolich/havalo-client/1.2">1.2</a>.

## Resolvers

If you wish to use this artifact, you can easily add it to your existing Maven or SBT project using <a href="https://github.com/markkolich/markkolich.github.com#marks-maven2-repository">my GitHub hosted Maven2 repository</a>.

### SBT

```scala
resolvers += "Kolich repo" at "http://markkolich.github.com/repo"

val havaloClient = "com.kolich" % "havalo-client" % "1.2" % "compile"
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
  <version>1.2</version>
  <scope>compile</scope>
</dependency>
```

## Usage

### Instantiate a HavaloClient

Before you can make API requests, you'll need to instantiate a `HavaloClient` backed by an `HttpClient` instance.

You will pass your Havalo API access key and secret to the `HavaloClient` via its constructor.  The `HavaloClient` instance will handle all authentication with the API on your behalf.  Further, you'll need to provide the Havalo API endpoint URL.

Note, the API key and secret are created by the Havalo API when a new repository is created.  

```java
import com.kolich.havalo.client.service.HavaloClient;
import java.util.UUID;

// Your Havalo API key
final UUID key = ...;

// Your Havalo API secret
final String secret = ...;

// Your Havalo API endpoint URL, usually something like
// http://localhost:8080/havalo/api
final String apiUrl = ...;

final HavaloClient client = new HavaloClient(key, secret, apiUrl);
```

If you have a configured `HttpClient` that you'd like to use instead of the default, you can of course use a slightly different constructor and pass your own.

```java
import org.apache.http.client.HttpClient;

final HttpClient httpClient = ...; // Your own HttpClient instance

final HavaloClient client = new HavaloClient(httpClient, key, secret, apiUrl);
```

Finally, if you're using Spring, your web-application can also instantiate a `HavaloClient` bean.

```xml
<!-- An HttpClient instance, either created by the KolichHttpClientFactory or on your own. -->
<bean id="YourHttpClient"
  class="com.kolich.http.blocking.KolichDefaultHttpClient.KolichHttpClientFactory"
  factory-method="getNewInstanceWithProxySelector">
  <constructor-arg><value>Some kewl user-agent</value></constructor-arg>
</bean>

<bean id="HavaloClient"
  class="com.kolich.havalo.client.service.HavaloClient">
  <constructor-arg index="0" ref="YourHttpClient" />
  <constructor-arg index="1"><value>6fe8e625-ec21-4890-a685-a7db4346cceb</value></constructor-arg>
  <constructor-arg index="2"><value>Crb7s5coXNb...EnQIYr-9cxNqShozksHitLg</value></constructor-arg>
  <constructor-arg index="3"><value>http://localhost:8080/havalo/api</value></constructor-arg>
</bean>
```

That's it, you're ready to make API requests.

### Using your HavaloClient

All `HavaloClient` methods return an `com.kolich.common.either.Either<F,S>` &mdash; this return type represents *either* a left type `F` indicating failure, or a right type `S` indicating success.  For more details on this return type and how to use it, please refer to the <a href="https://github.com/markkolich/kolich-httpclient4-closure#functional-concepts">Functional Concepts overview</a> in my <a href="https://github.com/markkolich/kolich-httpclient4-closure">kolich-httpclient4-closure</a> library.

#### authenticate()

Verify your Havalo API authentication credentials.

Does nothing other than verifies your API key and secret.  This is most useful on application startup when you want to verify connectivity/access to the Havalo API before attempting to do actual work. 

```java
final Either<HttpFailure,KeyPair> auth =
  client.authenticate();

if(auth.success()) {
  // Success
} else {
  // Failed
}
```

#### createRepository()

Create a new repository.

Note that only **administrator** level API users can create new repositories.

```java
final Either<HttpFailure,KeyPair> repo =
  client.createRepository();

final KeyPair kp;
if((kp = repo.right()) != null) {
  // Success
  System.out.println("Your new API key: " + kp.getKey());
  System.out.println("Your new API secret: " + kp.getSecret());
}
```

#### deleteRepository(toDelete)

Delete repository by UUID `toDelete`.

Note that only **administrator** level API users can delete repositories.

```java
final UUID toDelete = ...; // The UUID of the repository to delete.

final Either<HttpFailure,Integer> del =
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
final Either<HttpFailure,ObjectList> list =
  client.listObjects("foobar", "baz");

if(list.success()) {
  final ObjectList objs = list.right();
  System.out.println("Found " + objs.size() + " objects.");
}
```

#### getObject(outputStream, path...)

Get an object with the given `path` and write it out to the provided `outputStream`.

```java
final OutputStream os = ...; // An existing and open OutputStream.

// Get the object whose key is "foobar/baz/0.json" and stream its
// bytes to the provided OutputStream.
final Either<HttpFailure,List<Header>> get =
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

Or, pass a `CustomEntityConverter` provided by the <a href="https://github.com/markkolich/kolich-httpclient4-closure">kolich-httpclient4-closure</a> to stream the object "elsewhere" on success, or handle a failure on error.  This allows you to extract meta-data about the object from Havalo before streaming the object out to a consumer &mdash; properties like the HTTP `Content-Length` or `Content-Type` of the object are only available on a `GET` operation if you use a `CustomEntityConverter`.  

```java
import com.kolich.http.blocking.helpers.definitions.CustomEntityConverter;

import org.apache.commons.io.IOUtils;

final ServletResponse response = ...; // From your Servlet container

client.getObject(new CustomEntityConverter<HttpFailure,Long>() {
  @Override
  public Long success(final HttpSuccess success) throws Exception {
    // Send down the HTTP Content-Type as fetched in the response
    // from the Havalo K,V store.
    final String contentType;
    if((contentType = success.getContentType()) != null) {
      response.setContentType(contentType);
    }
    // Send down the HTTP Content-Length as fetched in the response
    // from the Havalo K,V store.
    final String contentLength;
    if((contentLength = success.getContentLength()) != null) {
      response.setContentLength(Integer.parseInt(contentLength));
    }
    // Actually stream the bytes out to the caller.
    final ServletOutputStream os = response.getOutputStream();
    return IOUtils.copyLarge(success.getContent(), os);
  }
  @Override
  public HttpFailure failure(final HttpFailure failure) {
    // Handle failure, write out error response, do whatever is
    // appropriate on error.
    // ...
    return failure;
  }
}, "foobar", "baz", "0.json");
```

Or, even further, you can pass a `CustomSuccessEntityConverter<S>` and a `CustomFailureEntityConverter<F>` to define separate units of work to be "called" on either success or failure.

```java
import com.kolich.http.blocking.helpers.definitions.CustomFailureEntityConverter;
import com.kolich.http.blocking.helpers.definitions.CustomSuccessEntityConverter;

client.getObject(
  // Success converter
  new CustomSuccessEntityConverter<S>() {
    @Override
    public Long success(final HttpSuccess success) throws Exception {
      // Do something on success, return type 'S'
    }
  },
  // Failure converter
  new CustomFailureEntityConverter<F>() {
    @Override
    public HttpFailure failure(final HttpFailure failure) {
      // Do something on failure, return type 'F' 
    }
  },
  // Path to object key
  "foobar", "baz", "0.json"
);
```

The intention of `CustomSuccessEntityConverter<S>` and `CustomFailureEntityConverter<F>` is to let you define reusable units of work &mdash; reusable implementations that define how to convert a response entity into something useful specific to your application, outside of an inline anonymous class. 

#### getObjectMetaData(path...)

Get the meta data associated with the object at the given `path`.

Any HTTP headers sent with the object during a `PUT` are returned.  Note that the API generated SHA-1 hash that was computed on upload is also returned in the `ETag` HTTP response header.

```java
// Get the meta data for the object whose key is "foobar/baz/1.xml"
final Either<HttpFailure,List<Header>> meta =
  client.getObjectMetaData("foobar", "baz", "1.xml");

if(meta.success()) {
  final List<Header> headers = meta.right();
  for(final Header h : headers) {
    System.out.println(h.getName() + ": " + h.getValue());
  }
}
```

#### putObject(inputStream, contentLength, headers, path...)

Upload (`PUT`) an object to the given `path` that is `contentLength` bytes long, using the provided `inputStream`.  Send any additional meta data represented by `headers` with the request too.

```java
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpHeaders.ETAG;

final InputStream is = ...; // An existing and open InputStream.

// Upload an object to path "baz/foobar.jpg" .. since it's a JPG
// image, also send the relevant Content-Type with the request.
final Either<HttpFailure,FileObject> upload =
  client.putObject(is,
    // The number of bytes in this object.
    1024L,
    // A Content-Type to be saved with the object.  This header is
    // sent back with the object when it's retrieved.
    new Header[]{new BasicHeader(CONTENT_TYPE, "image/jpeg")},
    // The path to the object.
    "baz", "foobar.jpg");

if(upload.success()) {
  // Success
  // The SHA-1 hash of the uploaded object can be found in the
  // ETag HTTP response header.
  final String hash = upload.right().getFirstHeader(ETAG);
  System.out.println("Uploaded object SHA-1 hash is: " + hash);
}
```

**TIP:** For a conditional `PUT`, you can also send an `If-Match` HTTP request header with your request.  If the SHA-1 hash sent with the `If-Match` header matches the current SHA-1 hash of the object, the object will be *replaced*.  If the SHA-1 hash sent with the `If-Match` header does *not* match the current hash of the object, the `PUT` will fail with a `409 Conflict`. 

#### putObject(byte[], path...)

Upload (`PUT`) an object to the given `path` using the provided `byte[]` array.

```java
final byte[] data = ...; // Some byte[] array of data.

// Upload an object to path "cat"
final Either<HttpFailure,FileObject> upload =
  client.putObject(data, "cat");

if(upload.success()) {
  // Success
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
final Either<HttpFailure,Integer> delete =
  client.deleteObject(
    new Header[]{new BasicHeader(IF_MATCH, myHash)},
    "foobar", "cat");

if(delete.success()) {
  // Success
} else {
  // Failed, get the resulting HTTP status code so
  // we can see what happened.
  switch(delete.left().getStatusCode()) {
    case 404:
      // 404 Not Found
      // Object at provided path didn't exist.
      // ...
      break;
    case 409:
      // 409 Conflict
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
final Either<HttpFailure,Integer> delete =
  client.deleteObject("foobar", "cat");

if(delete.success()) {
  // Success
} else {
  // Failed
  System.out.println("Oops, delete failed with status: " +
    delete.left().getStatusCode());
}
```

## Building

This Java library and its dependencies are built and managed using <a href="https://github.com/harrah/xsbt">SBT 0.12.2</a>.

To clone and build havalo-client, you must have <a href="http://www.scala-sbt.org/release/docs/Getting-Started/Setup">SBT installed and configured on your computer</a>.

The havalo-client SBT <a href="https://github.com/markkolich/havalo-client/blob/master/project/Build.scala">Build.scala</a> file is highly customized to build and package this Java artifact.  It's written to manage all dependencies and versioning.

To build, clone the repository.

    #~> git clone git://github.com/markkolich/havalo-client.git

Run SBT from within havalo-client.

    #~> cd havalo-client
    #~/havalo-client> sbt
    ...
    havalo-client:1.2>

You will see a `havalo-client` SBT prompt once all dependencies are resolved and the project is loaded.

In SBT, run `package` to compile and package the JAR.

    havalo-client:1.2> package
    [info] Compiling 12 Java sources to ~/havalo-client/target/classes...
    [info] Packaging ~/havalo-client/dist/havalo-client-1.2.jar ...
    [info] Done packaging.
    [success] Total time: 4 s, completed

Note the resulting JAR is placed into the **havalo-client/dist** directory.

To create an Eclipse Java project for havalo-client, run `eclipse` in SBT.

    havalo-client:1.2> eclipse
    ...
    [info] Successfully created Eclipse project files for project(s):
    [info] havalo-client

You'll now have a real Eclipse **.project** file worthy of an Eclipse import.

Note your new **.classpath** file as well &mdash; all source JAR's are fetched and injected into the Eclipse project automatically.

## Licensing

Copyright (c) 2012 <a href="http://mark.koli.ch">Mark S. Kolich</a>

All code in this artifact is freely available for use and redistribution under the <a href="http://opensource.org/comment/991">MIT License</a>.

See <a href="https://github.com/markkolich/havalo-client/blob/master/LICENSE">LICENSE</a> for details.
