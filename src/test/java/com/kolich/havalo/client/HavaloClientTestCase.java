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

package com.kolich.havalo.client;

import com.kolich.havalo.client.service.HavaloClient;
import com.kolich.havalo.client.service.HavaloClientCredentials;
import com.kolich.havalo.client.service.HavaloClientSigner;
import com.kolich.havalo.client.signing.HavaloAbstractSigner;
import com.kolich.havalo.client.signing.algorithms.HMACSHA256Signer;
import com.kolich.http.HttpConnector;
import com.kolich.http.KolichDefaultHttpClient.KolichHttpClientFactory;

public abstract class HavaloClientTestCase extends HavaloClientBaseTest {
	
	private static final String HAVALO_API_URL_PROPERTY = "havalo.apiUrl";
	private static final String HAVALO_ACCESS_KEY_PROPERTY = "havalo.key";
    private static final String HAVALO_SECRET_PROPERTY = "havalo.secret";
    
    /*
     * -Dhavalo.apiUrl=http://someurl.example.com -Dhavalo.key=AKIAJ5IHGCMPX2GWPWIQ -Dhavalo.secret=1234567890123456789012345678901234456789
     */
    
    protected final HavaloClientCredentials credentials_;
    protected final HttpConnector connector_;
    protected final HavaloAbstractSigner signer_;
    
    protected final HavaloClient client_;
	
	public HavaloClientTestCase() throws Exception {
		super();
		final String apiUrl = System.getProperty(HAVALO_API_URL_PROPERTY);
		final String key = System.getProperty(HAVALO_ACCESS_KEY_PROPERTY);
		final String secret = System.getProperty(HAVALO_SECRET_PROPERTY);
		if(apiUrl == null || key == null || secret == null) {
			throw new IllegalArgumentException("You're missing the " +
				"-Dhavalo.apiUrl or -Dhavalo.key or -Dhavalo.secret " +
					"required VM properties on your command line.");
		}
		// Setup the access credentials.
		credentials_ = new HavaloClientCredentials(key, secret);
		// Create a new Havalo HTTP connector with a pre-configured
		// HTTP client.
		connector_ = new HttpConnector(KolichHttpClientFactory
			.getNewInstanceWithProxySelector());
		// Create a new HMAC-SHA256 signer.
		signer_ = new HavaloClientSigner(credentials_, new HMACSHA256Signer());
		// Setup a new HavaloClient
		client_ = new HavaloClient(connector_, signer_, apiUrl);
	}
	
}
