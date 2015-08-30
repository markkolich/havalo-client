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

package com.kolich.havalo.client.signing.algorithms;

import com.kolich.havalo.client.HavaloClientException;
import com.kolich.havalo.client.signing.HavaloCredentials;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.apache.commons.codec.binary.Base64.encodeBase64;
import static org.apache.commons.codec.binary.StringUtils.getBytesUtf8;
import static org.apache.commons.codec.binary.StringUtils.newStringUtf8;

/**
 * Computes an HMAC-SHA256 signature.
 */
public final class HMACSHA256Signer implements HavaloSigningAlgorithm {
	
	private static final String HMAC_SHA256_ALGORITHM_NAME = "HmacSHA256";
		
	/**
     * Returns a Base-64 encoded HMAC-SHA256 signature.
     */
	@Override
	public String sign(final HavaloCredentials credentials,
		final String input) {
		String result = null;
		try {
			// Get a new instance of the HMAC-SHA256 algorithm.
			final Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM_NAME);
			// Init it with our secret and the secret-key algorithm.
			mac.init(new SecretKeySpec(getBytesUtf8(credentials.getSecret()), HMAC_SHA256_ALGORITHM_NAME));
			// Sign the input.
			result = newStringUtf8(encodeBase64(mac.doFinal(getBytesUtf8(input))));
		} catch (Exception e) {
			throw new HavaloClientException("Failed to SHA-256 sign input " +
				"string: " + input, e);
		}
		return result;
	}
	
}
