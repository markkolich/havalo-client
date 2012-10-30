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

import static org.apache.commons.io.IOUtils.LINE_SEPARATOR_UNIX;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.HttpHeaders.DATE;

import java.util.Date;

import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;

import com.kolich.common.date.RFC822DateFormat;
import com.kolich.havalo.client.signing.HavaloAbstractSigner;
import com.kolich.havalo.client.signing.HavaloCredentials;
import com.kolich.havalo.client.signing.algorithms.HavaloSigningAlgorithm;

public final class HavaloClientSigner extends HavaloAbstractSigner {
	
	public HavaloClientSigner(HavaloCredentials credentials,
		HavaloSigningAlgorithm signer) {
		super(credentials, signer);
	}
	
	// Expected format is ...
    //    Authorization: Havalo AccessKey:Signature
    // ... where the AccessKey is the unique UUID used to identify the user.
    // And, the Signature is ...
    //    Base64( HMAC-SHA256( UTF-8-Encoding-Of( AccessSecret, StringToSign ) ) );
    // And, the StringToSign is ....
    //    HTTP-Verb (GET, PUT, POST, or DELETE) + "\n" +
    //    RFC822 Date (from 'Date' header on request) + "\n" +
    //    CanonicalizedResource (the part of this request's URL from
    //        the protocol name up to the query string in the first line
    //        of the HTTP request)

	@Override
	public void signHttpRequest(final HttpRequestBase request) {
		// Add a Date header to the request.
    	request.addHeader(DATE, RFC822DateFormat.format(new Date()));
    	final String signature = signer_.sign(credentials_,
    		getStringToSign(request));
    	// Add the resulting Authorization header to the request.
		request.addHeader(AUTHORIZATION,
			// The format of the Authorization header ...
			String.format("Havalo %s:%s",
				// The Access Key ID uniquely identifies a Havalo user.
				credentials_.getKey(),
				// The computed Havalo auth signature for this request.
				signature));
	}
	
	private static final String getStringToSign(final HttpRequestBase request) {
		final StringBuilder sb = new StringBuilder();
		// HTTP-Verb (GET, PUT, POST, or DELETE) + "\n"
		sb.append(request.getMethod().toUpperCase())
			.append(LINE_SEPARATOR_UNIX);
		// RFC822 formatted Date (from 'Date' header on request) + "\n"		
		sb.append(request.getFirstHeader(DATE).getValue())
			.append(LINE_SEPARATOR_UNIX);
		// Content-Type (from 'Content-Type' request header, optional) + "\n"
		final Header contentType;
		if((contentType = request.getFirstHeader(CONTENT_TYPE)) != null) {
			sb.append(contentType.getValue());
		}
		sb.append(LINE_SEPARATOR_UNIX);
		// CanonicalizedResource
		sb.append(request.getURI().getRawPath());
		return sb.toString();
	}
	
}
