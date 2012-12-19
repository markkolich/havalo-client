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
import static com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory.getNewInstanceWithProxySelector;
import static org.apache.commons.io.IOUtils.copyLarge;
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

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kolich.havalo.client.entities.FileObject;
import com.kolich.havalo.client.entities.KeyPair;
import com.kolich.havalo.client.entities.ObjectList;
import com.kolich.havalo.client.signing.HavaloAbstractSigner;
import com.kolich.http.HttpClient4Closure;
import com.kolich.http.HttpClient4Closure.HttpFailure;
import com.kolich.http.HttpClient4Closure.HttpResponseEither;
import com.kolich.http.HttpClient4Closure.HttpSuccess;
import com.kolich.http.helpers.CustomEntityConverterOrHttpFailureClosure;
import com.kolich.http.helpers.GsonOrHttpFailureClosure;
import com.kolich.http.helpers.StatusCodeOrHttpFailureClosure;
import com.kolich.http.helpers.definitions.CustomEntityConverter;

public final class HavaloClient extends HavaloAbstractService {
		
	private static final String API_ACTION_AUTHENTICATE = "authenticate";
	private static final String API_ACTION_REPOSITORY = "repository";
	private static final String API_ACTION_OBJECT = "object";
	
	private static final String API_PARAM_STARTSWITH = "startsWith";
	
	private final HttpClient client_;
	private final GsonBuilder gson_;
	
	public HavaloClient(HttpClient client, HavaloAbstractSigner signer,
		GsonBuilder gson, String apiEndpoint) {
		super(signer, apiEndpoint);
		client_ = client;
		gson_ = gson;
	}
	
	public HavaloClient(HttpClient client, HavaloAbstractSigner signer,
		final String apiEndpoint) {
		this(client, signer, getDefaultGsonBuilder(), apiEndpoint);
	}
	
	public HavaloClient(HavaloAbstractSigner signer, final String apiEndpoint) {
		this(getNewInstanceWithProxySelector(), signer, apiEndpoint);
	}
	
	public HavaloClient(final HavaloClientCredentials credentials,
		final String apiEndpoint) {
		this(new HavaloClientSigner(credentials), apiEndpoint);
	}
	
	public HavaloClient(final String key, final String secret,
		final String apiEndpoint) {
		this(new HavaloClientCredentials(key, secret), apiEndpoint);
	}
	
	private abstract class HavaloBaseClosure<T>
		extends HttpClient4Closure<HttpFailure,T> {
		public HavaloBaseClosure(final HttpClient client) {
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
	}
		
	private abstract class HavaloGsonClosure<T>
		extends GsonOrHttpFailureClosure<T> {
		public HavaloGsonClosure(final HttpClient client, final Gson gson,
			final Class<T> clazz) {
			super(client, gson, clazz);
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
	}
	
	private abstract class HavaloStatusCodeClosure
		extends StatusCodeOrHttpFailureClosure {
		public HavaloStatusCodeClosure(final HttpClient client) {
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
	}
	
	private abstract class HavaloEntityConverterClosure<T>
		extends CustomEntityConverterOrHttpFailureClosure<T> {
		public HavaloEntityConverterClosure(final HttpClient client,
			final CustomEntityConverter<T> converter) {
			super(client, converter);
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
	}
	
	public HttpResponseEither<HttpFailure,KeyPair> authenticate() {
		return new HavaloGsonClosure<KeyPair>(client_, gson_.create(),
			KeyPair.class) {
			@Override
			public boolean check(final int statusCode) {
				// The POST of auth credentials is only successful when the
				// resulting status code is a 200 OK.  Any other status
				// code on the response is failure.
				return statusCode == SC_OK;
			}
		}.post(buildPath(API_ACTION_AUTHENTICATE));
	}
	
	public HttpResponseEither<HttpFailure,KeyPair> createRepository() {
		return new HavaloGsonClosure<KeyPair>(client_, gson_.create(),
			KeyPair.class) {
			@Override
			public boolean check(final int statusCode) {
				// The POST of a repository is only successful when the
				// resulting status code is a 201 Created.  Any other status
				// code on the response is failure.
				return statusCode == SC_CREATED;
			}
		}.post(buildPath(API_ACTION_REPOSITORY));
	}
	
	public HttpResponseEither<HttpFailure,Integer> deleteRepository(
		final UUID repoId) {
		return new HavaloStatusCodeClosure(client_) {
			@Override
			public boolean check(final int statusCode) {
				// The DELETE of a repository is only successful when the
				// resulting status code is a 204 No Content.  Any other
				// status code on the response is failure.
				return statusCode == SC_NO_CONTENT;
			}
		}.delete(buildPath(API_ACTION_REPOSITORY, repoId.toString()));
	}
	
	public HttpResponseEither<HttpFailure,ObjectList> listObjects(
		final String... path) {
		return new HavaloGsonClosure<ObjectList>(client_, gson_.create(), 
			ObjectList.class) {
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
		}.get(buildPath(API_ACTION_REPOSITORY));
	}
	
	public HttpResponseEither<HttpFailure,ObjectList> listObjects() {
		return listObjects((String[])null);
	}
	
	public HttpResponseEither<HttpFailure,Long> getObject(
		final OutputStream destination, final String... path) {
		return getObject(new CustomEntityConverter<Long>() {
			@Override
			public Long convert(final HttpSuccess success) throws Exception {
				return copyLarge(
					success.getResponse().getEntity().getContent(),
					destination);
			}
		}, path);
	}

	public <T> HttpResponseEither<HttpFailure,T> getObject(
		final CustomEntityConverter<T> converter, final String... path) {
		return new HavaloEntityConverterClosure<T>(client_, converter) {
			@Override
			public boolean check(final int statusCode) {
				// The GET of an object is only successful when the
				// resulting status code is a 200 OK.  Any other status
				// code on the response is failure.
				return statusCode == SC_OK;
			}
		}.get(buildPath(API_ACTION_OBJECT, path));
	}

	public HttpResponseEither<HttpFailure,List<Header>> getObjectMetaData(
		final String... path) {
		return new HavaloBaseClosure<List<Header>>(client_) {
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
		}.head(buildPath(API_ACTION_OBJECT, path));
	}
	
	public HttpResponseEither<HttpFailure,FileObject> putObject(
		final InputStream input, final long contentLength,
		final Header[] headers, final String... path) {
		return new HavaloGsonClosure<FileObject>(client_, gson_.create(),
			FileObject.class) {
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
		}.put(buildPath(API_ACTION_OBJECT, path));
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
		return new HavaloStatusCodeClosure(client_) {
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
		}.delete(buildPath(API_ACTION_OBJECT, path));
	}
	
	public HttpResponseEither<HttpFailure,Integer> deleteObject(
		final String... path) {
		return deleteObject(null, path);
	}
	
	private static final String buildPath(final String action,
		final String... path) {
		final StringBuilder sb = new StringBuilder(SLASH_STRING);
		sb.append(action);
		if(path != null) {
			sb.append(SLASH_STRING).append(urlEncode(varargsToPrefixString(path)));
		}
		return sb.toString();
	}
	
	private static final String buildPath(final String action) {
		return buildPath(action, (String[])null);
	}
	
}
