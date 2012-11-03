/**
 * Copyright (c) 2012 Mark S. Kolich
 * http://mark.koli.ch
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package com.kolich.havalo.client.api;

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.common.http.HttpConnectorResponse.consumeQuietly;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
import static org.apache.commons.lang3.RandomStringUtils.randomAscii;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpHeaders.IF_MATCH;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_METHOD_NOT_ALLOWED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.kolich.common.http.HttpConnectorResponse;
import com.kolich.havalo.client.HavaloClientTestCase;
import com.kolich.havalo.client.entities.FileObject;

public class PutTest extends HavaloClientTestCase {
	
	private static final String SAMPLE_JSON_OBJECT =
		"{\"dog\":\"cat\",\"integer\":2}";
	
	public PutTest() throws Exception {
		super();
	}
	
	@Test
	public void put() throws Exception {
		HttpConnectorResponse response = null;
		try {
			response = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json")},
					"test", "jsonObject/hmm");
			assertTrue("Failed to PUT sample object: " +
				response.getStatus(), response.getStatus() == SC_OK);
			// Validate JSON deserialization
			gson_.fromJson(EntityUtils.toString(response.getEntity(), UTF_8),
				FileObject.class);
			consumeQuietly(response);
			// GET the object to make sure it actually worked.
			response = client_.getObject("test", "jsonObject/hmm");
			assertTrue("Failed to GET sample object after PUT: " +
				response.getStatus(), response.getStatus() == SC_OK);
			final String received = EntityUtils.toString(response.getEntity(),
				UTF_8);
			assertTrue("Object received on GET did not match object " +
				"sent on PUT (sent="+ SAMPLE_JSON_OBJECT + ", received=" +
					received + ")", received.equals(SAMPLE_JSON_OBJECT));
			consumeQuietly(response);
			// Tear down
			response = client_.deleteObject("test", "jsonObject/hmm");
			assertTrue("Failed to DELETE sample object: " +
				response.getStatus(), response.getStatus() == SC_NO_CONTENT);
		} finally {
			consumeQuietly(response);
		}
	}
	
	@Test
	public void putLongName() throws Exception {
		HttpConnectorResponse response = null;
		try {
			// Trying to stay under the default Tomcat maxHttpHeaderSize of 8KB
			// http://stackoverflow.com/questions/6837505/setting-max-http-header-size-with-ajp-tomcat-6-0
			// http://stackoverflow.com/questions/1730158/how-to-set-the-ajp-packet-size-in-tomcat
			// Apparently the max header size in Jetty is less around 4KB
			final String[] prefixes = getRandomPrefix(200);
			response = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json")},
					prefixes);
			assertTrue("Failed to PUT long sample object: " +
				response.getStatus(), response.getStatus() == SC_OK);
			// Validate JSON deserialization
			gson_.fromJson(EntityUtils.toString(response.getEntity(), UTF_8),
				FileObject.class);
			consumeQuietly(response);
			// Tear down
			response = client_.deleteObject(prefixes);
			assertTrue("Failed to DELETE long sample object: " +
				response.getStatus(), response.getStatus() == SC_NO_CONTENT);
		} finally {
			consumeQuietly(response);
		}
	}
	
	@Test
	public void putWithSpacesInName() throws Exception {
		HttpConnectorResponse response = null;
		try {
			final String[] prefixes = new String[]{"   ////// ", "kewl",
				"    should still totally work  ++++ ^^^^ && foobar"}; 
			response = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json")},
					prefixes);
			assertTrue("Failed to PUT object with crap in name: " +
				response.getStatus(), response.getStatus() == SC_OK);
			// Validate JSON deserialization
			gson_.fromJson(EntityUtils.toString(response.getEntity(), UTF_8),
				FileObject.class);
			consumeQuietly(response);
			// Tear down
			response = client_.deleteObject(prefixes);
			assertTrue("Failed to DELETE object with crap in name: " +
				response.getStatus(), response.getStatus() == SC_NO_CONTENT);
		} finally {
			consumeQuietly(response);
		}
	}
	
	@Test
	public void putNoName() throws Exception {
		HttpConnectorResponse response = null;
		try {
			response = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json")},
					"");
			assertTrue("Failed to PUT object with no name: " +
				response.getStatus(), response.getStatus() ==
					SC_METHOD_NOT_ALLOWED);
		} finally {
			consumeQuietly(response);
		}
	}
	
	@Test
	public void putWithCorrectETag() throws Exception {
		HttpConnectorResponse response = null;
		try {
			final String[] prefixes = getRandomPrefix(50);
			response = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json")},
					prefixes);
			assertTrue("Failed to PUT sample object: " +
				response.getStatus(), response.getStatus() == SC_OK);
			// Validate JSON deserialization
			gson_.fromJson(EntityUtils.toString(response.getEntity(), UTF_8),
				FileObject.class);
			consumeQuietly(response);
			// Extract the ETag HTTP response header from the PUT.
			final String eTag = response.getHeaderValue(HttpHeaders.ETAG);
			assertNotNull("ETag header on PUT was null.", eTag);
			// Do another put with the correct If-Match ETag.
			response = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json"),
					// Object in repo must match incoming ETag
					new BasicHeader(IF_MATCH, eTag)},
					prefixes);
			assertTrue("Failed to PUT sample object with correct eTag: " +
				response.getStatus(), response.getStatus() == SC_OK);
			// Validate JSON deserialization
			gson_.fromJson(EntityUtils.toString(response.getEntity(), UTF_8),
				FileObject.class);
			consumeQuietly(response);
			// Tear down
			response = client_.deleteObject(prefixes);
			assertTrue("Failed to DELETE long sample object: " +
				response.getStatus(), response.getStatus() == SC_NO_CONTENT);
		} finally {
			consumeQuietly(response);
		}
	}
	
	@Test
	public void putWithConflictingETag() throws Exception {
		HttpConnectorResponse response = null;
		try {
			final String[] prefixes = getRandomPrefix(50);
			response = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json")},
					prefixes);
			assertTrue("Failed to PUT sample object: " + response.getStatus(),
				response.getStatus() == SC_OK);
			// Validate JSON deserialization
			gson_.fromJson(EntityUtils.toString(response.getEntity(), UTF_8),
				FileObject.class);
			consumeQuietly(response);
			// Extract the ETag HTTP response header from the PUT.
			final String eTag = response.getHeaderValue(HttpHeaders.ETAG);
			assertNotNull("ETag header on PUT was null.", eTag);
			// Do another put with a totally wrong ETag in the If-Match
			// request header (SHOULD FAIL).
			response = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json"),
					// Totally non-sense ETag -- should fail.
					new BasicHeader(IF_MATCH, "totallybogusetag")},
					prefixes);
			assertTrue("PUT with known bad ETag did not return a 409 " +
				"Conflict as expected: " + response.getStatus(),
					response.getStatus() == SC_CONFLICT);
			consumeQuietly(response);
			// Tear down
			response = client_.deleteObject(prefixes);
			assertTrue("Failed to DELETE long sample object: " +
				response.getStatus(), response.getStatus() == SC_NO_CONTENT);
		} finally {
			consumeQuietly(response);
		}
	}

	private static final String[] getRandomPrefix(final int length) {
		final String[] prefixes = new String[length];
		for(int i = 0, l = prefixes.length; i < l; i++) {
			prefixes[i] = randomAscii(10);
		}
		return prefixes;
	}
	
}
