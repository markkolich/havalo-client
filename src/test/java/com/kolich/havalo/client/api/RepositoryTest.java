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

import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.junit.Test;

import com.kolich.havalo.client.HavaloClientTestCase;
import com.kolich.havalo.client.entities.FileObject;
import com.kolich.havalo.client.entities.KeyPair;
import com.kolich.havalo.client.entities.ObjectList;
import com.kolich.http.HttpClient4Closure.HttpFailure;
import com.kolich.http.HttpClient4Closure.HttpResponseEither;

public class RepositoryTest extends HavaloClientTestCase {
	
	private static final String SAMPLE_JSON_OBJECT = "{\"foo\":\"bar\"}";
	
	public RepositoryTest() throws Exception {
		super();
	}
	
	@Test
	public void createAndDeleteRepository() throws Exception {
		// Create a sample repository
		final HttpResponseEither<HttpFailure,KeyPair> create =
			client_.createRepository();
		assertTrue("Failed to create repository.", create.success());
		// Tear down, delete it
		final HttpResponseEither<HttpFailure,Integer> delete =
			client_.deleteRepository(create.right().getKey());
		assertTrue("Failed to delete repository.", delete.success());
	}
	
	@Test
	public void deleteAdminRepository() throws Exception {
		// Attempt to delete the "admin" repository, should fail.
		final HttpResponseEither<HttpFailure,Integer> delete =
			client_.deleteRepository(UUID.fromString(apiKey_));
		assertFalse("Uh, successfully deleted admin repository?",
			delete.success());
		// Validate that the API called failed in the "right way".
		if(!delete.success()) {
			assertTrue("Expected a 403 Forbidden when deleting admin " +
				"repository, but got: " + delete.left().getStatusCode(),
				delete.left().getStatusCode() == SC_FORBIDDEN);
		}
	}
	
	@Test
	public void deleteNonExistentRepository() throws Exception {
		// Attempt to delete a non-existent repository, should fail.
		final HttpResponseEither<HttpFailure,Integer> delete =
			client_.deleteRepository(UUID.randomUUID());
		assertFalse("Uh, successfully deleted non-existent repository?",
			delete.success());
	}
	
	@Test
	public void listObjects() throws Exception {
		// PUT some sample objects with predictable names
		putSampleObjects();
		// List ALL objects in the repo
		ObjectList list = listObjects((String[])null);
		// Validate that the number of objects we got back match what
		// we put into the repo
		assertTrue("Object count did not match",
			list.getObjectList().size() == 3);
		// Only list objects that start with "randomness" (this shouldn't
		// match anything)
		list = listObjects(Long.toString(System.currentTimeMillis()),
			"random.index");
		assertTrue("Object count was not zero (empty)",
			list.getObjectList().size() == 0);
		// Should be (2) objects that start with "foo"
		list = listObjects("foo");
		assertTrue("Object count for startsWith='foo' was not 2",
			list.getObjectList().size() == 2);
		// Should be (1) object that starts with "foobar/bar"
		list = listObjects("foobar", "bar");
		assertTrue("Object count for startsWith='foobar/bar' was not 1",
			list.getObjectList().size() == 1);
		// DELETE all sample objects
		deleteSampleObjects();
	}
	
	private final void putSampleObjects() {
		HttpResponseEither<HttpFailure,FileObject> put = null;
		// Sample objects
		put = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
			new Header[]{new BasicHeader(CONTENT_TYPE, "application/json")},
				"json", "object");
		assertTrue("Failed to put sample object #1.", put.success());
		put = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
			new Header[]{new BasicHeader(CONTENT_TYPE, "text/plain")},
				"foo", "bar.json");
		assertTrue("Failed to put sample object #2.", put.success());
		put = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
			new Header[]{new BasicHeader(CONTENT_TYPE, "image/gif")},
				"foobar", "bar.gif");
		assertTrue("Failed to put sample object #3.", put.success());
	}
	
	private final ObjectList listObjects(final String... path) throws Exception {
		HttpResponseEither<HttpFailure, ObjectList> list =
			client_.listObjects(path);
		assertTrue("Failed to list objects in repository.", list.success());
		return list.right();
	}
	
	private final void deleteSampleObjects() {
		HttpResponseEither<HttpFailure,Integer> delete = null;
		// Delete sample objects.
		delete = client_.deleteObject("json", "object");
		assertTrue("Failed to DELETE sample object #1.", delete.success());
		delete = client_.deleteObject("foo", "bar.json");
		assertTrue("Failed to DELETE sample object #2.", delete.success());
		delete = client_.deleteObject("foobar", "bar.gif");
		assertTrue("Failed to DELETE sample object #3.", delete.success());
	}
	
}
