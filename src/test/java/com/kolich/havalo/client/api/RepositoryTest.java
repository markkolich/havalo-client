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
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertTrue;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import com.kolich.common.http.HttpConnectorResponse;
import com.kolich.havalo.client.HavaloClientTestCase;
import com.kolich.havalo.client.entities.KeyPair;
import com.kolich.havalo.client.entities.ObjectList;

public class RepositoryTest extends HavaloClientTestCase {
	
	private static final String SAMPLE_JSON_OBJECT = "{\"foo\":\"bar\"}";
	
	public RepositoryTest() throws Exception {
		super();
	}
	
	@Test
	public void createRepository() throws Exception {
		HttpConnectorResponse response = null;
		try {
			response = client_.createRepository();
			assertTrue("Failed to create repository: " +
				response.getStatus(), response.getStatus() == SC_CREATED);
			gson_.fromJson(EntityUtils.toString(response.getEntity(), UTF_8),
				KeyPair.class);
			/*
			final KeyPair kp = gson_.fromJson(EntityUtils.toString(response.getEntity(), UTF_8),
				KeyPair.class);
			System.out.println(kp.getKey().toString() + " " + kp.getSecret());
			*/
		} finally {
			consumeQuietly(response);
		}
	}
	
	@Test
	public void listObjects() throws Exception {
		HttpConnectorResponse response = null;
		ObjectList list = null;
		try {
			// PUT some sample objects with predictable names
			putSampleObjects();
			// List ALL objects in the repo
			list = listObjects((String[])null);
			// Validate that the number of objects we got back match what
			// we put into the repo
			assertTrue("Object count did not match",
				list.getObjectList().size() == 3);
			// Only list objects that start with "randomness" (this shouldn't
			// match anything)
			list = listObjects(Long.toString(System.currentTimeMillis()),
				"random.bin");
			assertTrue("Object count was not zero (empty)",
				list.getObjectList().size() == 0);
			list = listObjects("foo");
			assertTrue("Object count for startsWith='foo' was not 2",
				list.getObjectList().size() == 2);
			list = listObjects("foobar", "bar");
			assertTrue("Object count for startsWith='foobar/bar' was not 1",
				list.getObjectList().size() == 1);
			// DELETE all sample objects
			deleteSampleObjects();
		} finally {
			consumeQuietly(response);
		}
	}
	
	private void putSampleObjects() {
		HttpConnectorResponse response = null;
		try {
			// PUT some sample objects with predictable names
			response = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
				new Header[]{new BasicHeader(CONTENT_TYPE, "application/json")},
					"json", "object");
			assertTrue("Failed to put sample object: " +
				response.getStatus(), response.getStatus() == SC_OK);
			consumeQuietly(response);
			response = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
				new Header[]{new BasicHeader(CONTENT_TYPE, "text/plain")},
					"foo", "bar.json");
			assertTrue("Failed to put sample object: " +
				response.getStatus(), response.getStatus() == SC_OK);
			consumeQuietly(response);
			response = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
				new Header[]{new BasicHeader(CONTENT_TYPE, "image/gif")},
					"foobar", "bar.gif");
			assertTrue("Failed to put sample object: " +
				response.getStatus(), response.getStatus() == SC_OK);
			consumeQuietly(response);
		} finally {
			consumeQuietly(response);
		}
	}
	
	private ObjectList listObjects(final String... path) throws Exception {
		HttpConnectorResponse response = null;
		try {
			response = client_.listObjects(path);
			assertTrue("Failed to list objects in repository: " +
				response.getStatus(), response.getStatus() == SC_OK);
			return gson_.fromJson(EntityUtils.toString(response.getEntity(),
				UTF_8), ObjectList.class);
		} finally {
			consumeQuietly(response);
		}
	}
	
	private void deleteSampleObjects() {
		HttpConnectorResponse response = null;
		try {
			// Tear down
			response = client_.deleteObject("json", "object");
			assertTrue("Failed to DELETE sample object: " +
				response.getStatus(), response.getStatus() == SC_NO_CONTENT);
			consumeQuietly(response);
			response = client_.deleteObject("foo", "bar.json");
			assertTrue("Failed to DELETE sample object: " +
				response.getStatus(), response.getStatus() == SC_NO_CONTENT);
			consumeQuietly(response);
			response = client_.deleteObject("foobar", "bar.gif");
			assertTrue("Failed to DELETE sample object: " +
				response.getStatus(), response.getStatus() == SC_NO_CONTENT);
			consumeQuietly(response);
		} finally {
			consumeQuietly(response);
		}
	}
	
}
