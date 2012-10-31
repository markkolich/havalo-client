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

import static com.kolich.common.http.HttpConnectorResponse.consumeQuietly;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.kolich.common.http.HttpConnectorResponse;
import com.kolich.havalo.client.HavaloClientTestCase;

public class DeleteTest extends HavaloClientTestCase {
	
	private static final String SAMPLE_JSON_OBJECT = "{}";
	
	public DeleteTest() throws Exception {
		super();
	}
	
	@Before
	public void setup() {
		HttpConnectorResponse response = null;
		try {
			response = client_.putObject(getBytesUtf8(SAMPLE_JSON_OBJECT),
				"test", "object.json");
			assertTrue("Failed to PUT sample object, response code: " +
				response.getStatus(), response.getStatus() == SC_OK);
		} finally {
			consumeQuietly(response);
		}
	}
	
	@After
	public void teardown() {
		HttpConnectorResponse response = null;
		try {
			// Ignore errors, if this delete fails, whatever it's part of a
			// local tear down for this DELETE test case.
			response = client_.deleteObject("test", "object.json");
		} finally {
			consumeQuietly(response);
		}
	}

	@Test
	public void delete() throws Exception {
		HttpConnectorResponse response = null;
		try {
			response = client_.deleteObject("test", "object.json");
			assertTrue("Failed to DELETE sample object, response code: " +
				response.getStatus(), response.getStatus() == SC_NO_CONTENT);
		} finally {
			consumeQuietly(response);
		}
	}
	
	@Test
	public void deleteNotFound() throws Exception {
		HttpConnectorResponse response = null;
		try {
			response = client_.deleteObject("totalbougusobject.jpg");
			assertTrue("Deletion status of non-existent object wasn't 404",
				response.getStatus() == SC_NOT_FOUND);
		} finally {
			consumeQuietly(response);
		}
	}
		
}
