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

import com.kolich.common.functional.either.Either;
import com.kolich.havalo.client.HavaloClientTestCase;
import com.kolich.havalo.client.entities.FileObject;
import com.kolich.http.common.response.HttpFailure;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeleteTest extends HavaloClientTestCase {
	
	private static final String SAMPLE_JSON_OBJECT = "{}";
	
	public DeleteTest() throws Exception {
		super();
	}
	
	@Before
	public void setup() {
		final Either<HttpFailure,FileObject> response =
			client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
				"test", "object.json");
		assertTrue("Failed to PUT sample object.", response.success());		
	}
	
	@After
	public void teardown() {
		// Ignore errors, if this delete fails, whatever it's part of a
		// local tear down for this DELETE test case.
		client_.deleteObject("test", "object.json");
	}

	@Test
	public void delete() throws Exception {
		final Either<HttpFailure,Integer> response =
			client_.deleteObject("test", "object.json");
		assertTrue("Failed to DELETE sample object.",
			response.success());
		assertTrue("Failed to DELETE sample object, response code: " +
			response.right(), response.right() == SC_NO_CONTENT);
	}
	
	@Test
	public void deleteNotFound() throws Exception {
		final Either<HttpFailure,Integer> response =
			client_.deleteObject("totalbougusobject.jpg");
		assertFalse("Deletion status of non-existent object was successful?",
			response.success());
		assertTrue("Deletion status of non-existent object wasn't 404",
			response.left().getStatusCode() == SC_NOT_FOUND);
	}
		
}
