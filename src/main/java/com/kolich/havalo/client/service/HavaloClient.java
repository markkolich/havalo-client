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
import static com.kolich.common.entities.KolichCommonEntity.getDefaultGsonBuilder;
import static com.kolich.common.util.URLEncodingUtils.urlEncode;
import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_NO_CONTENT;
import static org.apache.http.HttpStatus.SC_OK;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import com.google.gson.GsonBuilder;
import com.kolich.havalo.client.HavaloClientException;
import com.kolich.havalo.client.entities.FileObject;
import com.kolich.havalo.client.entities.KeyPair;
import com.kolich.havalo.client.entities.ObjectList;
import com.kolich.havalo.client.signing.HavaloAbstractSigner;
import com.kolich.http.HttpClient4Closure;
import com.kolich.http.HttpClient4Closure.HttpFailure;
import com.kolich.http.HttpClient4Closure.HttpResponseEither;

public final class HavaloClient extends HavaloAbstractService {
		
	private static final String API_ACTION_AUTHENTICATE = "authenticate";
	private static final String API_ACTION_REPOSITORY = "repository";
	private static final String API_ACTION_OBJECT = "object";
	
	private static final String API_PARAM_STARTSWITH = "startsWith";
	
	private final HttpClient client_;
	private final GsonBuilder gson_;
	
	public HavaloClient(HttpClient client,
		HavaloAbstractSigner signer, String apiEndpoint) {
		super(signer, apiEndpoint);
		client_ = client;
		gson_ = getDefaultGsonBuilder();
	}
	
	private abstract class HavaloHttpClient4Closure<T>
		extends HttpClient4Closure<HttpFailure,T> {
		public HavaloHttpClient4Closure(final HttpClient client) {
			super(client);
		}
		@Override
		public void before(final HttpRequestBase request) {
			signRequest(request);
		}
		@Override
		public boolean check(final HttpResponse response,
			final HttpContext context) {
			return check(response.getStatusLine().getStatusCode());
		}
		public abstract boolean check(final int statusCode);
		@Override
		public abstract T success(final HttpSuccess success) throws Exception;
		@Override
		public HttpFailure failure(final HttpFailure failure) {
			return failure;
		}
	}

	public HttpResponseEither<HttpFailure,KeyPair> authenticate() {
		return new HavaloHttpClient4Closure<KeyPair>(client_) {
			@Override
			public boolean check(final int statusCode) {
				// The POST of auth credentials is only successful when the
				// resulting status code is a 200 OK.  Any other status
				// code on the response is failure.
				return statusCode == SC_OK;
			}
			@Override
			public KeyPair success(final HttpSuccess success) throws Exception {
				return gson_.create().fromJson(
					responseToString(success.getResponse()),
					KeyPair.class);
			}
		}.request(new HttpPost(SLASH_STRING + API_ACTION_AUTHENTICATE));
	}
	
	public HttpResponseEither<HttpFailure,KeyPair> createRepository() {
		return new HavaloHttpClient4Closure<KeyPair>(client_) {
			@Override
			public boolean check(final int statusCode) {
				// The POST of a repository is only successful when the
				// resulting status code is a 201 Created.  Any other status
				// code on the response is failure.
				return statusCode == SC_CREATED;
			}
			@Override
			public KeyPair success(final HttpSuccess success) throws Exception {
				return gson_.create().fromJson(
					responseToString(success.getResponse()),
					KeyPair.class);
			}
		}.request(new HttpPost(SLASH_STRING + API_ACTION_REPOSITORY));
	}
	
	public HttpResponseEither<HttpFailure,Integer> deleteRepository(final UUID repoId) {
		return new HavaloHttpClient4Closure<Integer>(client_) {
			@Override
			public boolean check(final int statusCode) {
				// The DELETE of a repository is only successful when the
				// resulting status code is a 200 OK.  Any other status
				// code on the response is failure.
				return statusCode == SC_NO_CONTENT;
			}
			@Override
			public Integer success(final HttpSuccess success) {
				return success.getResponse().getStatusLine().getStatusCode();
			}
		}.request(new HttpDelete(SLASH_STRING + API_ACTION_REPOSITORY +
			SLASH_STRING + repoId));
	}
	
	public HttpResponseEither<HttpFailure,ObjectList> listObjects(final String... path) {
		return new HavaloHttpClient4Closure<ObjectList>(client_) {
			@Override
			public void before(final HttpRequestBase request) {
				final List<NameValuePair> params = new ArrayList<NameValuePair>();
				if(path != null && path.length > 0) {
					params.add(new BasicNameValuePair(API_PARAM_STARTSWITH,
						varargsToPrefixString(path)));
				}
				// Update the URI to include the "?query=" parameters.
				request.setURI(URI.create(request.getURI().toString() +
					QUERY_STRING + URLEncodedUtils.format(params, UTF_8)));
				super.before(request);
			}
			@Override
			public boolean check(final int statusCode) {
				// The listing of objects is only successful when the
				// resulting status code is a 200 OK.  Any other status
				// code on the response is failure.
				return statusCode == SC_OK;
			}
			@Override
			public ObjectList success(final HttpSuccess success) throws Exception {
				return gson_.create().fromJson(
					responseToString(success.getResponse()),
					ObjectList.class);
			}
		}.request(new HttpGet(SLASH_STRING + API_ACTION_REPOSITORY));
	}
	
	public HttpResponseEither<HttpFailure,ObjectList> listObjects() {
		return listObjects((String[])null);
	}
	
	public HttpResponseEither<HttpFailure,Long> getObject(
		final OutputStream destination, final String... path) {
		return new HavaloHttpClient4Closure<Long>(client_) {
			@Override
			public boolean check(final int statusCode) {
				// The GET of an object is only successful when the
				// resulting status code is a 200 OK.  Any other status
				// code on the response is failure.
				return statusCode == SC_OK;
			}
			@Override
			public Long success(final HttpSuccess success) throws Exception {
				return IOUtils.copyLarge(
					success.getResponse().getEntity().getContent(),
					destination);
			}
		}.request(new HttpGet(SLASH_STRING + API_ACTION_OBJECT + SLASH_STRING +
			urlEncode(varargsToPrefixString(path))));
	}
	
	public HttpResponseEither<HttpFailure,List<Header>> getObjectMetaData(
		final String... path) {
		return new HavaloHttpClient4Closure<List<Header>>(client_) {
			@Override
			public boolean check(final int statusCode) {
				// The HEAD of an object is only successful when the
				// resulting status code is a 200 OK.  Any other status
				// code on the response is failure.
				return statusCode == SC_OK;
			}
			@Override
			public List<Header> success(final HttpSuccess success) {
				return Arrays.asList(success.getResponse().getAllHeaders());
			}
		}.request(new HttpHead(SLASH_STRING + API_ACTION_OBJECT + SLASH_STRING +
			urlEncode(varargsToPrefixString(path))));
	}
	
	public HttpResponseEither<HttpFailure,FileObject> putObject(
		final InputStream input, final long contentLength,
		final Header[] headers, final String... path) {
		return new HavaloHttpClient4Closure<FileObject>(client_) {
			@Override
			public void before(final HttpRequestBase request) {
				if(headers != null) {
					request.setHeaders(headers);
				}
				((HttpPut)request).setEntity(new InputStreamEntity(input,
					contentLength));
				super.before(request);
			}
			@Override
			public boolean check(final int statusCode) {
				// The upload of an object is only successful when the
				// resulting status code is a 200 OK.  Any other status
				// code on the response is failure.
				return statusCode == SC_OK;
			}
			@Override
			public FileObject success(final HttpSuccess success) throws Exception {
				return gson_.create().fromJson(
					responseToString(success.getResponse()),
					FileObject.class);
			}
		}.request(new HttpPut(SLASH_STRING + API_ACTION_OBJECT + SLASH_STRING +
			urlEncode(varargsToPrefixString(path))));
	}
			
	public HttpResponseEither<HttpFailure,FileObject> putObject(
		final byte[] input, final Header[] headers, final String... path) {
		final InputStream is = new ByteArrayInputStream(input);
		return putObject(is, (long)input.length, headers, path);
	}
	
	public HttpResponseEither<HttpFailure,FileObject> putObject(
		final byte[] input, final String... path) {
		return putObject(input, null, path);
	}
		
	public HttpResponseEither<HttpFailure,Integer> deleteObject(
		final Header[] headers, final String... path) {
		return new HavaloHttpClient4Closure<Integer>(client_) {
			@Override
			public void before(final HttpRequestBase request) {
				if(headers != null) {
					request.setHeaders(headers);
				}
				super.before(request);
			}
			@Override
			public boolean check(final int statusCode) {
				// The deletion of an object is only successful when the
				// resulting status code is a 204 No Content.  Any other status
				// code on the response is failure.
				return statusCode == SC_NO_CONTENT;
			}
			@Override
			public Integer success(final HttpSuccess success) {
				return success.getResponse().getStatusLine().getStatusCode();
			}
		}.request(new HttpDelete(SLASH_STRING + API_ACTION_OBJECT +
			SLASH_STRING + urlEncode(varargsToPrefixString(path))));
	}
	
	public HttpResponseEither<HttpFailure,Integer> deleteObject(
		final String... path) {
		return deleteObject(null, path);
	}
	
	private static final String responseToString(final HttpResponse response) {
		try {
			return EntityUtils.toString(response.getEntity(), UTF_8);
		} catch (Exception e) {
			throw new HavaloClientException("Failed to parse entity to " +
				"String.", e);
		}
	}

}
