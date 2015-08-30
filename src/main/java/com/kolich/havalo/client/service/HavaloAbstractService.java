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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.kolich.common.DefaultCharacterEncoding.UTF_8;
import static java.util.regex.Pattern.quote;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.client.methods.HttpRequestBase;

import com.kolich.havalo.client.HavaloClientException;
import com.kolich.havalo.client.signing.HavaloAbstractSigner;

public abstract class HavaloAbstractService {
	
	protected static final String HTTP = "http://";
	protected static final String HTTPS = "https://";
	
	protected static final String HTTP_SCHEME_SLASHES = "://";
	
	protected static final String SLASH_STRING = "/";
	protected static final String EMPTY_STRING = "";
	protected static final String QUERY_STRING = "?";
	protected static final String DOT_STRING = ".";
		
	protected final HavaloAbstractSigner signer_;
	
	/**
	 * The Havalo API endpoint that this service communicates with.
	 */
	protected final URI apiEndpoint_;
	
	public HavaloAbstractService(HavaloAbstractSigner signer, String apiEndpoint) {
		checkNotNull(signer, "The signer cannot be null!");
		checkNotNull(apiEndpoint, "The service client API endpoint cannot " +
			"be null!");
		signer_ = signer;
		apiEndpoint_ = URI.create(apiEndpoint);
	}
	
	/**
	 * Prepares and signs the request.
	 * @param request the request object
	 */
	protected final void signRequest(final HttpRequestBase request) {
		checkNotNull(request, "Request cannot be null!");
		// Compute the final endpoint for the request and set it.
		request.setURI(getFinalEndpoint(request));
		// Sign the request using an appropriate request signer.		
		signer_.signHttpRequest(request);
	}
	
	private final URI getFinalEndpoint(final HttpRequestBase request) {
		URI endPointURI = request.getURI();
		// If the request URI already starts with https:// then we don't
		// have to build a full endpoint URL anymore since its already
		// been provided.  This assumes the caller knows what they are
		// doing and have built a complete and proper URL to the API.
		if(!isComplete(endPointURI)) {
			endPointURI = URI.create(
				// Havalo API endpoints usually start with https://	
				apiEndpoint_.getScheme() + HTTP_SCHEME_SLASHES +
				// Returns the decoded authority component of this endpoint URI.
				// The authority of a URI is basically the hostname, otherwise
				// called the endpoint here.
				apiEndpoint_.getAuthority() +
				apiEndpoint_.getPath() +
				// Returns the decoded path component of the request URI.
				// The path of a URI is the piece of the URI after the hostname,
				// not including the query parameters.
				getPath(endPointURI) +
				// Returns the decoded query component of this URI.
				// The query parameters, if any.
				getQuery(endPointURI));
		}
		return endPointURI;
	}
	
	/**
	 * Given a {@link URI} returns the path component of that
	 * {@link URI}.  The path of a URI is the piece of the URI after
	 * the hostname, not including the query parameters.  If the URI
	 * is null, a single "/" is returned.  If the URI is not null, but
	 * the path is empty, an "" empty string is returned.
	 * @param uri the URI to extract the path from
	 * @return
	 */
	private static final String getPath(final URI uri) {
		if(uri == null) {
			return SLASH_STRING;
		} else {
			final String path = uri.getRawPath();
			return (path == null) ? EMPTY_STRING : path;
		}
	}
	
	/**
	 * Given a {@link URI} returns the query string component of that
	 * {@link URI}.  The query of a URI is the piece after the "?". If the
	 * URI is null, an "" empty string is returned.  If the URI is not null,
	 * but the query is empty, an "" empty string is returned.
	 * @param uri the URI to extract the query from
	 * @return
	 */
	private static final String getQuery(final URI uri) {
		if(uri == null) {
			return EMPTY_STRING;
		} else {
			final String query = uri.getRawQuery();
			return (query == null) ? EMPTY_STRING : QUERY_STRING + query;
		}
	}
	
	/**
	 * Checks if the given URI is non-null, and if it's a complete endpoint
	 * URI that already starts with "https://" or "http://".
	 * @param uri
	 * @return
	 */
	private static final boolean isComplete(final URI uri) {
		return uri != null && (uri.toString().startsWith(HTTPS) ||
			uri.toString().startsWith(HTTP));
	}
	
	/**
	 * Given a variable list of arguments, prepare a fully qualified
	 * path to a key in a repository.  Each prefix in the list is
	 * separated by an appropriate path separator.  The resulting string
	 * is NOT URL-encoded, but each prefix component is URL-encoded before
	 * concatenated to the resulting path -- slashes and other special
	 * characters in a prefix component that may be interpreted wrong when
	 * used in a path are URL-encoded so we won't have any conflicts.
	 * Note that empty strings in the varargs prefix list will NOT be appended
	 * to the resulting prefix string.
	 * Example:
	 * <code>
	 * new String[]{"accounts", "", "silly/path+dog"}
	 * </code>
	 * is returned as
	 * <code>
	 * "accounts/silly%2Fpath%2Bdog"
	 * </code>
	 * @param prefixes
	 * @return
	 */
	public static final String varargsToPrefixString(final String... prefixes) {
		checkNotNull(prefixes, "The prefix list cannot be null!");
		try {
			final StringBuilder sb = new StringBuilder();
			for(int i = 0, l = prefixes.length; i < l; i++) {
				if(!EMPTY_STRING.equals(prefixes[i])) {
					sb.append(URLEncoder.encode(prefixes[i], UTF_8));
					// Don't append a "/" if this element is the last in the list.
					sb.append((i < l-1) ? SLASH_STRING : EMPTY_STRING);
				}
			}
			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			throw new HavaloClientException(e);
		}
	}
	
	/**
	 * Given a prefix string, generated by
	 * {@link HavaloAbstractService#varargsToPrefixString(String...)}, returns
	 * a variable arguments compatible String[] array containing each prefix
	 * component.  Each component in the resulting prefix String[] array will be
	 * fully URL-decoded.  Note that any empty strings, once the prefix string
	 * is split around a path separator, are NOT added to the resulting
	 * varargs list.
	 * @param prefix
	 * @return
	 */
	public static final String[] prefixStringToVarargs(final String prefix) {
		checkNotNull(prefix, "The prefix string cannot be null!");
		try {
			final List<String> prl = new ArrayList<String>();
			for(final String p : prefix.split(quote(SLASH_STRING))) {
				if(!EMPTY_STRING.equals(p)) {
					prl.add(URLEncoder.encode(p, UTF_8));
				}
			}
			return prl.toArray(new String[]{});
		} catch (UnsupportedEncodingException e) {
			throw new HavaloClientException(e);
		}
	}
	
	/**
	 * Appends the given key to the end of the prefix list, then returns a
	 * a new String[] array representing that list.
	 * @param key
	 * @param prefixes
	 * @return
	 */
	public static final String[] appendKeyToPrefixes(final String key,
		final String... prefixes) {
		checkNotNull(key, "The key to append cannot be null!");
		checkNotNull(prefixes, "The prefix list cannot be null!");
		final List<String> prl = new ArrayList<String>(Arrays.asList(prefixes));
		// The "key" becomes the last element in the prefix list.
    	prl.add(key);
    	return prl.toArray(new String[]{});
	}
	
}
