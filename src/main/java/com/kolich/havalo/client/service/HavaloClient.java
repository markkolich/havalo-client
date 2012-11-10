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

package com.kolich.havalo.client.service;

import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static com.kolich.common.util.URLEncodingUtils.urlEncode;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicNameValuePair;

import com.kolich.havalo.client.signing.HavaloAbstractSigner;
import com.kolich.http.HttpConnector;
import com.kolich.http.HttpConnectorResponse;

public final class HavaloClient extends HavaloAbstractService {
		
	private static final String API_ACTION_AUTHENTICATE = "authenticate";
	private static final String API_ACTION_REPOSITORY = "repository";
	private static final String API_ACTION_OBJECT = "object";
	
	private static final String API_PARAM_STARTSWITH = "startsWith";
	
	public HavaloClient(HttpConnector connector,
		HavaloAbstractSigner signer, String apiEndpoint) {
		super(connector, signer, apiEndpoint);
	}

	public HttpConnectorResponse authenticate() {
		return doMethod(new HttpPost(SLASH_STRING + API_ACTION_AUTHENTICATE));
	}
	
	public HttpConnectorResponse createRepository() {
		return doMethod(new HttpPost(SLASH_STRING + API_ACTION_REPOSITORY));
	}
	
	public HttpConnectorResponse deleteRepository(final UUID repoId) {
		return doMethod(new HttpDelete(SLASH_STRING + API_ACTION_REPOSITORY +
			SLASH_STRING + repoId));
	}
	
	public HttpConnectorResponse listObjects(final String... path) {
		final List<NameValuePair> params = new ArrayList<NameValuePair>();
		if(path != null && path.length > 0) {
			params.add(new BasicNameValuePair(API_PARAM_STARTSWITH,
				varargsToPrefixString(path)));
		}
		final HttpGet get = new HttpGet(SLASH_STRING +
			API_ACTION_REPOSITORY + QUERY_STRING +
			URLEncodedUtils.format(params, UTF_8));
		return doMethod(get);
	}
	
	public HttpConnectorResponse listObjects() {
		return listObjects((String[])null);
	}
	
	public HttpConnectorResponse getObject(final String... path) {
		final HttpGet get = new HttpGet(SLASH_STRING +
			API_ACTION_OBJECT + SLASH_STRING +
			urlEncode(varargsToPrefixString(path)));
		return doMethod(get);
	}
	
	public HttpConnectorResponse getObjectMetaData(final String... path) {
		final HttpHead head = new HttpHead(SLASH_STRING +
			API_ACTION_OBJECT + SLASH_STRING +
			urlEncode(varargsToPrefixString(path)));
		return doMethod(head);
	}
	
	public HttpConnectorResponse putObject(final InputStream input,
		final long contentLength, final Header[] headers,
		final String... path) {
		final HttpPut put = new HttpPut(SLASH_STRING +
			API_ACTION_OBJECT + SLASH_STRING +
			urlEncode(varargsToPrefixString(path)));
		if(headers != null) {
			put.setHeaders(headers);
		}
		put.setEntity(new InputStreamEntity(input, contentLength));
		return doMethod(put);
	}
			
	public HttpConnectorResponse putObject(final byte[] input,
		final Header[] headers, final String... path) {
		final InputStream is = new ByteArrayInputStream(input);
		return putObject(is, (long)input.length, headers, path);
	}
	
	public HttpConnectorResponse putObject(final byte[] input,
		final String... path) {
		return putObject(input, null, path);
	}
		
	public HttpConnectorResponse deleteObject(final Header[] headers,
		final String... path) {
		final HttpDelete delete = new HttpDelete(SLASH_STRING +
			API_ACTION_OBJECT + SLASH_STRING +
			urlEncode(varargsToPrefixString(path)));
		if(headers != null) {
			delete.setHeaders(headers);
		}
		return doMethod(delete);
	}
	
	public HttpConnectorResponse deleteObject(final String... path) {
		return deleteObject(null, path);
	}

}
